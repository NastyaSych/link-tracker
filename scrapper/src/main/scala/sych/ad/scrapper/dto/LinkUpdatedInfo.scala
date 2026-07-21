package sych.ad.scrapper.dto

import java.sql.Timestamp
import java.time.Instant

sealed trait LinkUpdatedInfo {
  def updatedAt: Instant
  def title: String
  def userName: String
  def preview: String
}

sealed trait GithubEventType
case object PullRequest extends GithubEventType
case object Issue       extends GithubEventType

object LinkTableTypes {
  type linkInfoType = (Long, String, Option[Timestamp], Option[String], Option[String], Option[String], Option[String])
}

case class GithubUpdatedInfo(
    updatedAt: Instant,
    title: String,
    eventType: GithubEventType,
    userName: String,
    preview: String
) extends LinkUpdatedInfo

case class StackOverflowUpdatedInfo(
    updatedAt: Instant,
    title: String,
    userName: String,
    preview: String
) extends LinkUpdatedInfo
