package sych.ad.scrapper.client.github

import sttp.tapir.Schema
import sych.ad.scrapper.dto.GithubEventType
import tethys._
import tethys.derivation.semiauto._

import java.time.Instant

case class GithubUser(login: String)

case class PullRequest(url: String)

case class GithubIssueResponse(
    title: String,
    user: GithubUser,
    updated_at: String,
    body: Option[String],
    pull_request: Option[PullRequest]
) {
  def updatedAt: Instant = Instant.parse(updated_at)
}

object GithubIssueResponse {

  implicit val schema: Schema[GithubEventType]    = Schema.derivedEnumeration[GithubEventType].defaultStringBased
  implicit val userReader: JsonReader[GithubUser] = jsonReader[GithubUser]
  implicit val userWriter: JsonWriter[GithubUser] = jsonWriter[GithubUser]
  implicit val prReader: JsonReader[PullRequest]  = jsonReader[PullRequest]
  implicit val prWriter: JsonWriter[PullRequest]  = jsonWriter[PullRequest]
  implicit val responseReader: JsonReader[GithubIssueResponse] = jsonReader[GithubIssueResponse]
  implicit val responseWriter: JsonWriter[GithubIssueResponse] = jsonWriter[GithubIssueResponse]
}
