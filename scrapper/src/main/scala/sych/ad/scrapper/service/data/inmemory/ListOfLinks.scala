package sych.ad.scrapper.service.data.inmemory

import cats.effect.IO
import cats.effect.kernel.Ref
import sych.ad.scrapper.dto.{Link, LinkUpdatedInfo}

class ListOfLinks(ref: Ref[IO, List[Link]]) {

  def add(url: String, updateInfo: Option[LinkUpdatedInfo] = None): IO[Link] = {
    for {
      currentLinks <- ref.get
      nextId: Long = currentLinks.lastOption.fold(1L)(_.id + 1)
      newLink      = Link(nextId, url, updateInfo)
      _ <- ref.update(_ :+ newLink)

    } yield newLink
  }

  def remove(url: String): IO[Option[Link]] = {
    for {
      current <- ref.get
      linkToRemove = current.find(_.url == url)
      _ <- ref.update(_.filterNot(_.url == url))
    } yield linkToRemove
  }

  def getAll: IO[List[Link]] = ref.get

  def get(url: String): IO[Option[Link]] = ref.get.map(_.find(_.url == url))

  def update(url: String, updateInfo: LinkUpdatedInfo): IO[Option[Link]] = {
    for {
      current <- ref.get
      updatedLinks = current.map { link =>
        if (link.url == url) link.copy(updateInfo = Some(updateInfo))
        else link
      }
      _ <- ref.set(updatedLinks)
    } yield current.find(_.url == url)
  }

  def updateById(linkId: Long, updateInfo: LinkUpdatedInfo): IO[Option[Link]] = {
    for {
      current <- ref.get
      updatedLinks = current.map { link =>
        if (link.id == linkId) link.copy(updateInfo = Some(updateInfo))
        else link
      }
      _ <- ref.set(updatedLinks)
    } yield current.find(_.id == linkId)
  }
}

object ListOfLinks {
  def make: IO[ListOfLinks] = {
    Ref.of[IO, List[Link]](List.empty).map(new ListOfLinks(_))
  }
}
