package sych.ad.scrapper.client.github

import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.tethysjson.jsonBody
import sych.ad.common.dto.ApiErrorResponse

object GithubEndpoints {

  // (user, repo, state, sort, direction, per_page)
  type IssuesInput = (String, String, String, String, String, String, String)
  val issues: Endpoint[
    Unit,
    IssuesInput,
    ApiErrorResponse,
    List[GithubIssueResponse],
    Any
  ] =
    endpoint
      .get
      .in("repos" / path[String]("user") / path[String]("repo") / "issues")
      .in(header[String]("Authorization"))
      .in(query[String]("state").default("all"))
      .in(query[String]("sort").default("updated"))
      .in(query[String]("direction").default("desc"))
      .in(query[String]("per_page").default("10"))
      .errorOut(jsonBody[ApiErrorResponse])
      .out(jsonBody[List[GithubIssueResponse]])
}
