package sych.ad.scrapper.database

import sych.ad.scrapper.dto.{GithubUpdatedInfo, Issue, LinkUpdatedInfo, PullRequest, StackOverflowUpdatedInfo}

import java.sql.Timestamp

object LinkInfoMapper {

  def fromDatabase(
      updatedAt: Option[Timestamp],
      title: Option[String],
      userName: Option[String],
      preview: Option[String],
      eventType: Option[String]
  ): Option[LinkUpdatedInfo] = {
    for {
      ua <- updatedAt
      t  <- title
      un <- userName
      p  <- preview
    } yield {
      eventType match {
        case Some("PR") =>
          GithubUpdatedInfo(ua.toInstant, t, PullRequest, un, p)
        case Some("Issue") =>
          GithubUpdatedInfo(ua.toInstant, t, Issue, un, p)
        case _ =>
          StackOverflowUpdatedInfo(ua.toInstant, t, un, p)
      }
    }
  }

  def toDatabase(info: LinkUpdatedInfo)
      : (Option[Timestamp], Option[String], Option[String], Option[String], Option[String]) = {
    (
      Some(Timestamp.from(info.updatedAt)),
      Some(info.title),
      Some(info.userName),
      Some(info.preview),
      info match {
        case g: GithubUpdatedInfo        => Some(g.eventType.toString)
        case _: StackOverflowUpdatedInfo => None
      }
    )
  }
}
