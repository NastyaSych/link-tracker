package sych.ad.scrapper.service.data

import cats.effect.IO
import sych.ad.scrapper.dto.{LinkUpdatedInfo, User, Link => ScrapperLink}

trait LinksRepository {
  def addUser(user: User): IO[Unit]
  def deleteUserWithLinks(user: User): IO[Unit]
  def addLink(user: User, url: String, updateInfo: Option[LinkUpdatedInfo]): IO[ScrapperLink]
  def deleteLink(user: User, url: String): IO[ScrapperLink]
  def getLink(user: User, url: String): IO[Option[ScrapperLink]]
  def getLinks(user: User): IO[List[ScrapperLink]]
  def getUsersWithLinks(): IO[Map[User, List[ScrapperLink]]]
  def updateLink(user: User, linkId: Long, updateInfo: LinkUpdatedInfo): IO[Unit]
}
