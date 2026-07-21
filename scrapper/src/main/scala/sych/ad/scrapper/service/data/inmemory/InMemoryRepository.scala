package sych.ad.scrapper.service.data.inmemory

import cats.effect.IO
import cats.effect.kernel.Ref
import cats.implicits._
import sych.ad.scrapper.dto.{LinkUpdatedInfo, User, Link => ScrapperLink}
import sych.ad.scrapper.service.data.LinksRepository

class InMemoryRepository extends LinksRepository {

  private val storage: Ref[IO, Map[User, ListOfLinks]] =
    Ref.unsafe(Map.empty)

  override def addUser(user: User): IO[Unit] = {
    getUserLinks(user).void
  }

  override def deleteUserWithLinks(user: User): IO[Unit] = {
    storage.update(_ - user)
  }

  private def getUserLinks(user: User): IO[ListOfLinks] = {
    storage.get.flatMap { map =>
      map.get(user) match {
        case Some(links) => IO.pure(links)
        case None        =>
          for {
            newLinks <- ListOfLinks.make
            _        <- storage.update(_ + (user -> newLinks))
          } yield newLinks
      }
    }
  }

  override def addLink(user: User, url: String, updateInfo: Option[LinkUpdatedInfo]): IO[ScrapperLink] = {
    for {
      linksList <- getUserLinks(user)
      link      <- linksList.add(url, updateInfo)
    } yield ScrapperLink(link.id, link.url, link.updateInfo)
  }

  override def deleteLink(user: User, url: String): IO[ScrapperLink] = {
    for {
      linksList <- getUserLinks(user)
      maybeLink <- linksList.remove(url)
      link      <- maybeLink match {
        case Some(link) => IO.pure(ScrapperLink(link.id, link.url, link.updateInfo))
        case None       =>
          IO.pure(ScrapperLink(0, url, None))
      }
    } yield link
  }

  override def getLink(user: User, url: String): IO[Option[ScrapperLink]] = {
    for {
      linksList <- getUserLinks(user)
      maybeLink <- linksList.get(url)
    } yield maybeLink.map(link => ScrapperLink(link.id, link.url, link.updateInfo))
  }

  override def getLinks(user: User): IO[List[ScrapperLink]] = {
    for {
      linksList <- getUserLinks(user)
      links     <- linksList.getAll
    } yield links.map(link => ScrapperLink(link.id, link.url, link.updateInfo))
  }

  override def getUsersWithLinks(): IO[Map[User, List[ScrapperLink]]] = {
    for {
      map    <- storage.get
      result <- map.toList.traverse { case (user, linksList) =>
        linksList.getAll.map(links =>
          user -> links.map(link => ScrapperLink(link.id, link.url, link.updateInfo))
        )
      }
    } yield result.toMap
  }

  override def updateLink(user: User, linkId: Long, updateInfo: LinkUpdatedInfo): IO[Unit] = {
    for {
      linksList <- getUserLinks(user)
      _         <- linksList.updateById(linkId, updateInfo)
    } yield ()
  }
}
