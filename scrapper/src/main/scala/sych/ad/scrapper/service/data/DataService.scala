package sych.ad.scrapper.service.data

import cats.effect.IO
import cats.implicits._
import org.typelevel.log4cats.LoggerFactory
import sych.ad.common.dto.{AddLinkRequest, LinkResponse, ListLinksResponse, RemoveLinkRequest}
import sych.ad.scrapper.dto.{Link, User}
import sych.ad.scrapper.service.ScrappingService

import java.time.Instant

trait DataService {
  def registerChat(userId: Long, tgChatId: Long): IO[Unit]
  def deleteChat(userId: Long, tgChatId: Long): IO[Unit]
  def getLinks(userId: Long, tgChatId: Long): IO[ListLinksResponse]
  def addLink(userId: Long, tgChatId: Long, addLinkRequest: AddLinkRequest): IO[LinkResponse]
  def deleteLink(userId: Long, tgChatId: Long, removeLinkRequest: RemoveLinkRequest): IO[LinkResponse]
  def patchUserLinks(user: User, linksToPatch: List[Link]): IO[Unit]
  def getLink(user: User, url: String): IO[Option[Link]]
  def getUsersWithLinks(): IO[Map[User, List[Link]]]

}

class DataServiceImpl(
    linksRepository: LinksRepository,
    scrappingService: ScrappingService
)(implicit L: LoggerFactory[IO]) extends DataService {
  private val logger = L.getLogger

  override def registerChat(userId: Long, tgChatId: Long): IO[Unit] =
    linksRepository.addUser(User(userId, tgChatId))

  override def deleteChat(userId: Long, tgChatId: Long): IO[Unit] =
    linksRepository.deleteUserWithLinks(User(userId, tgChatId))

  override def getLinks(userId: Long, tgChatId: Long): IO[ListLinksResponse] =
    linksRepository.getLinks(User(userId, tgChatId)).map { links =>
      ListLinksResponse(links.map(_.toLinkResponse()), links.size)
    }

  override def getUsersWithLinks(): IO[Map[User, List[Link]]] =
    linksRepository.getUsersWithLinks()

  override def getLink(user: User, url: String): IO[Option[Link]] =
    linksRepository.getLink(user, url)

  override def patchUserLinks(user: User, linksToPatch: List[Link]): IO[Unit] =
    linksToPatch.traverse_(link =>
      link.updateInfo.fold(IO.unit)(info => linksRepository.updateLink(user, link.id, info))
    )

  override def addLink(
      userId: Long,
      tgChatId: Long,
      addLinkRequest: AddLinkRequest
  ): IO[LinkResponse] = {
    val user = User(userId, tgChatId)

    for {
      updateInfo <-
        scrappingService.lastUpdate(addLinkRequest.link, Instant.EPOCH).map(_.headOption).handleError(_ => None)
      _    <- logger.info(s"GitHub last update for ${addLinkRequest.link}: $updateInfo")
      link <- linksRepository.addLink(user, addLinkRequest.link, updateInfo)
      _    <- logger.info(s"Saved link: id=${link.id}, url=${link.url}, updateInfo=${link.updateInfo}")
    } yield LinkResponse(
      id = link.id,
      url = link.url,
      wasBefore = false
    )
  }

  override def deleteLink(
      userId: Long,
      tgChatId: Long,
      removeLinkRequest: RemoveLinkRequest
  ): IO[LinkResponse] = {
    val user = User(userId, tgChatId)

    for {
      link <- linksRepository.deleteLink(user, removeLinkRequest.link)
      linkResponse = link.toLinkResponse(wasBefore = true)
    } yield linkResponse
  }
}

object DataService {

  def apply(linksRepository: LinksRepository, scrappingService: ScrappingService)(implicit
      L: LoggerFactory[IO]
  ): DataService =
    new DataServiceImpl(linksRepository, scrappingService)

  def resource(linksRepository: LinksRepository, scrappingService: ScrappingService)(implicit
      L: LoggerFactory[IO]
  ): IO[DataService] =
    IO.pure(new DataServiceImpl(linksRepository, scrappingService))
}
