package sych.ad.scrapper.service.data.orm

import cats.effect.IO
import slick.jdbc.PostgresProfile.api._
import sych.ad.scrapper.database.LinkInfoMapper
import sych.ad.scrapper.dto.{Link => ScrapperLink, LinkUpdatedInfo, User}
import sych.ad.scrapper.service.data.LinksRepository
import sych.ad.scrapper.service.data.orm.tables.{LinksTable, UserLinksTable, UsersTable}

import java.sql.Timestamp
import java.time.Instant
import scala.concurrent.ExecutionContext

class SlickRepository(db: Database)(implicit ec: ExecutionContext) extends LinksRepository {

  private val users     = UsersTable.query
  private val links     = LinksTable.query
  private val userLinks = UserLinksTable.query

  private def now(): Timestamp = Timestamp.from(Instant.now())

  override def addUser(user: User): IO[Unit] = {
    val action = users += (user.userId, user.tgChatId)
    IO.fromFuture(IO(db.run(action))).void
  }

  override def deleteUserWithLinks(user: User): IO[Unit] = {
    val action = users
      .filter(u => u.userId === user.userId && u.tgChatId === user.tgChatId)
      .delete
    IO.fromFuture(IO(db.run(action))).void
  }

  override def getLinks(user: User): IO[List[ScrapperLink]] = {
    val query = for {
      ul <- userLinks if ul.userId === user.userId && ul.tgChatId === user.tgChatId
      l  <- links if l.id === ul.linkId
    } yield (
      l.id,
      l.url,
      l.lastUpdatedAt,
      l.lastUpdateTitle,
      l.lastUpdateUserName,
      l.lastUpdatePreview,
      l.lastUpdateEventType
    )

    IO.fromFuture(IO(db.run(query.result))).map { rows =>
      rows.map { case (id, url, updatedAt, title, userName, preview, eventType) =>
        val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
        ScrapperLink(id, url, updateInfo)
      }.toList
    }
  }

  override def addLink(user: User, url: String, updateInfo: Option[LinkUpdatedInfo]): IO[ScrapperLink] = {
    val action = (for {
      linkId <- links.filter(_.url === url).map(_.id).result.headOption.flatMap {
        case Some(existingId) => DBIO.successful(existingId)
        case None             =>
          val (ua, title, userName, preview, eventType) =
            updateInfo.map(LinkInfoMapper.toDatabase).getOrElse((None, None, None, None, None))
          links.map(l =>
            (
              l.url,
              l.lastUpdatedAt,
              l.lastUpdateTitle,
              l.lastUpdateUserName,
              l.lastUpdatePreview,
              l.lastUpdateEventType
            )
          )
            .returning(links.map(_.id))
            .+=((url, ua, title, userName, preview, eventType))
      }

      _ <- userLinks.map(ul => (ul.userId, ul.tgChatId, ul.linkId, ul.createdAt))
        .+=((user.userId, user.tgChatId, linkId, now()))

      _ <- updateInfo match {
        case Some(info) =>
          val (ua, title, userName, preview, eventType) = LinkInfoMapper.toDatabase(info)
          links.filter(_.id === linkId).map(l =>
            (l.lastUpdatedAt, l.lastUpdateTitle, l.lastUpdateUserName, l.lastUpdatePreview, l.lastUpdateEventType)
          ).update((ua, title, userName, preview, eventType))
        case None => DBIO.successful(0)
      }
    } yield ScrapperLink(linkId, url, updateInfo)).transactionally

    IO.fromFuture(IO(db.run(action)))
  }

  override def deleteLink(user: User, url: String): IO[ScrapperLink] = {
    val action = (for {
      maybeLink <- (for {
        ul <- userLinks if ul.userId === user.userId && ul.tgChatId === user.tgChatId
        l  <- links if l.id === ul.linkId && l.url === url
      } yield (
        l.id,
        l.url,
        l.lastUpdatedAt,
        l.lastUpdateTitle,
        l.lastUpdateUserName,
        l.lastUpdatePreview,
        l.lastUpdateEventType
      )).result.headOption

      result <- maybeLink match {
        case Some((linkId, url, updatedAt, title, userName, preview, eventType)) =>
          val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
          userLinks
            .filter(ul => ul.userId === user.userId && ul.tgChatId === user.tgChatId && ul.linkId === linkId)
            .delete
            .map(_ => ScrapperLink(linkId, url, updateInfo))
        case None =>
          DBIO.successful(ScrapperLink(0, url, None))
      }
    } yield result).transactionally

    IO.fromFuture(IO(db.run(action)))
  }

  override def getLink(user: User, url: String): IO[Option[ScrapperLink]] = {
    val query = for {
      ul <- userLinks if ul.userId === user.userId && ul.tgChatId === user.tgChatId
      l  <- links if l.id === ul.linkId && l.url === url
    } yield (
      l.id,
      l.url,
      l.lastUpdatedAt,
      l.lastUpdateTitle,
      l.lastUpdateUserName,
      l.lastUpdatePreview,
      l.lastUpdateEventType
    )

    IO.fromFuture(IO(db.run(query.result.headOption))).map { maybeRow =>
      maybeRow.map { case (id, url, updatedAt, title, userName, preview, eventType) =>
        val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
        ScrapperLink(id, url, updateInfo)
      }
    }
  }

  override def getUsersWithLinks(): IO[Map[User, List[ScrapperLink]]] = {
    val query = for {
      ul <- userLinks
      l  <- links if l.id === ul.linkId
    } yield (
      ul.userId,
      ul.tgChatId,
      l.id,
      l.url,
      l.lastUpdatedAt,
      l.lastUpdateTitle,
      l.lastUpdateUserName,
      l.lastUpdatePreview,
      l.lastUpdateEventType
    )

    IO.fromFuture(IO(db.run(query.result))).map { rows =>
      rows.groupBy { case (userId, chatId, _, _, _, _, _, _, _) => User(userId, chatId) }
        .map { case (user, userRows) =>
          user -> userRows.map { case (_, _, linkId, url, updatedAt, title, userName, preview, eventType) =>
            val updateInfo = LinkInfoMapper.fromDatabase(updatedAt, title, userName, preview, eventType)
            ScrapperLink(linkId, url, updateInfo)
          }.toList
        }
    }
  }

  override def updateLink(user: User, linkId: Long, updateInfo: LinkUpdatedInfo): IO[Unit] = {
    val action = (for {
      exists <- userLinks
        .filter(ul => ul.userId === user.userId && ul.tgChatId === user.tgChatId && ul.linkId === linkId)
        .exists
        .result
      _ <- if (exists) {
        val (ua, title, userName, preview, eventType) = LinkInfoMapper.toDatabase(updateInfo)
        links.filter(_.id === linkId).map(l =>
          (l.lastUpdatedAt, l.lastUpdateTitle, l.lastUpdateUserName, l.lastUpdatePreview, l.lastUpdateEventType)
        ).update((ua, title, userName, preview, eventType))
      } else DBIO.successful(0)
    } yield ()).transactionally

    IO.fromFuture(IO(db.run(action))).void
  }
}
