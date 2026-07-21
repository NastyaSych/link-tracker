package sych.ad.scrapper.service.data.sql

import cats.effect.IO
import doobie._
import doobie.implicits._
import sych.ad.scrapper.database.LinkInfoMapper
import sych.ad.scrapper.dto.{GithubUpdatedInfo, LinkTableTypes, LinkUpdatedInfo, User, Link => ScrapperLink}
import sych.ad.scrapper.service.data.LinksRepository

import java.sql.Timestamp
import java.time.Instant

class PostgresRepository(xa: Transactor[IO]) extends LinksRepository {

  implicit val instantPut: Put[Instant] = Put[Timestamp].contramap(Timestamp.from)
  implicit val instantGet: Get[Instant] = Get[Timestamp].map(_.toInstant)

  override def addUser(user: User): IO[Unit] =
    sql"""
      INSERT INTO users (user_id, tg_chat_id) VALUES (${user.userId}, ${user.tgChatId})
      ON CONFLICT (user_id, tg_chat_id) DO NOTHING
    """.update.run.transact(xa).void

  override def deleteUserWithLinks(user: User): IO[Unit] =
    sql"""
      DELETE FROM users WHERE user_id = ${user.userId} AND tg_chat_id = ${user.tgChatId}
    """.update.run.transact(xa).void

  override def getLinks(user: User): IO[List[ScrapperLink]] =
    sql"""
        SELECT l.id, l.url, l.last_updated_at, l.last_update_title,
           l.last_update_user_name,
           l.last_update_preview,
           l.last_update_event_type
        FROM links l
        JOIN user_links ul ON l.id = ul.link_id
        WHERE ul.user_id = ${user.userId} AND ul.tg_chat_id = ${user.tgChatId}
      """.query[LinkTableTypes.linkInfoType].to[List]
      .map { rows =>
        rows.map { case (id, url, updatedAt, title, userName, preview, eventType) =>
          val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
          ScrapperLink(id, url, updateInfo)
        }
      }
      .transact(xa)

  override def addLink(user: User, url: String, updateInfo: Option[LinkUpdatedInfo]): IO[ScrapperLink] = {
    (for {
      linkId <-
        sql"""
        INSERT INTO links (url, last_updated_at, last_update_title, last_update_user_name, last_update_preview, last_update_event_type)
        VALUES (
        $url,
        ${updateInfo.map(_.updatedAt)},
        ${updateInfo.map(_.title)},
        ${updateInfo.map(_.userName)},
        ${updateInfo.map(_.preview)},
        ${updateInfo.flatMap {
            case g: GithubUpdatedInfo => Some(g.eventType.toString)
            case _                    => None
          }}
      )
      ON CONFLICT (url) DO UPDATE SET
        last_updated_at = EXCLUDED.last_updated_at,
        last_update_title = EXCLUDED.last_update_title,
        last_update_user_name = EXCLUDED.last_update_user_name,
        last_update_preview = EXCLUDED.last_update_preview,
        last_update_event_type = EXCLUDED.last_update_event_type
      RETURNING id
    """.query[Long].unique

      _ <- sql"""
        INSERT INTO user_links (user_id, tg_chat_id , link_id, created_at)
      VALUES (${user.userId}, ${user.tgChatId}, $linkId, NOW())
      ON CONFLICT DO NOTHING
    """.update.run
    } yield ScrapperLink(linkId, url, updateInfo)).transact(xa)
  }

  override def deleteLink(user: User, url: String): IO[ScrapperLink] = {
    (for {
      maybeLink <- sql"""
        SELECT l.id, l.url, l.last_updated_at, l.last_update_title,
               l.last_update_user_name, l.last_update_preview, l.last_update_event_type
        FROM links l
        JOIN user_links ul ON l.id = ul.link_id
        WHERE ul.user_id = ${user.userId} AND ul.tg_chat_id = ${user.tgChatId} AND l.url = $url
      """.query[LinkTableTypes.linkInfoType].option
      result <- maybeLink match {
        case Some((id, url, updatedAt, title, userName, preview, eventType)) =>
          val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
          sql"DELETE FROM user_links WHERE user_id = ${user.userId} AND tg_chat_id = ${user.tgChatId} AND link_id = $id".update.run
            .map(_ => ScrapperLink(id, url, updateInfo))
        case None =>
          FC.raiseError[ScrapperLink](new NoSuchElementException(s"Link not found: $url"))
      }
    } yield result).transact(xa)
  }

  override def getLink(user: User, url: String): IO[Option[ScrapperLink]] = {
    sql"""
        SELECT l.id, l.url, l.last_updated_at, l.last_update_title,
             l.last_update_user_name, l.last_update_preview, l.last_update_event_type
        FROM links l
        JOIN user_links ul ON l.id = ul.link_id
        WHERE ul.user_id = ${user.userId} AND ul.tg_chat_id = ${user.tgChatId} AND l.url = $url
      """.query[LinkTableTypes.linkInfoType].option
      .map { maybeRow =>
        maybeRow.map { case (id, url, updatedAt, title, userName, preview, eventType) =>
          val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
          ScrapperLink(id, url, updateInfo)
        }
      }
      .transact(xa)
  }

  override def getUsersWithLinks(): IO[Map[User, List[ScrapperLink]]] = {
    sql"""
      SELECT ul.user_id, ul.tg_chat_id,
             l.id, l.url,
             l.last_updated_at, l.last_update_title,
             l.last_update_user_name, l.last_update_preview, l.last_update_event_type
      FROM user_links ul
      JOIN links l ON ul.link_id = l.id
    """.query[(
        Long,
        Long,
        Long,
        String,
        Option[Timestamp],
        Option[String],
        Option[String],
        Option[String],
        Option[String]
    )].to[List]
      .map { rows =>
        rows.groupBy { case (userId, chatId, _, _, _, _, _, _, _) => User(userId, chatId) }
          .map { case (user, userRows) =>
            user -> userRows.map { case (_, _, linkId, url, updatedAt, title, userName, preview, eventType) =>
              val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
              ScrapperLink(linkId, url, updateInfo)
            }.toList
          }
      }
      .transact(xa)
  }

  override def updateLink(user: User, linkId: Long, updateInfo: LinkUpdatedInfo): IO[Unit] = {
    val (ua, title, userName, preview, eventType) = LinkInfoMapper.toDatabase(updateInfo)
    sql"""
      UPDATE links
      SET
        last_updated_at = $ua,
        last_update_title = $title,
        last_update_user_name = $userName,
        last_update_preview = $preview,
        last_update_event_type = $eventType
      WHERE id = $linkId
      AND EXISTS (
        SELECT 1 FROM user_links
        WHERE user_id = ${user.userId} AND tg_chat_id = ${user.tgChatId} AND link_id = $linkId
      )
    """.update.run.transact(xa).void
  }
}
