package sych.ad.scrapper.client.stackoverflow

import sttp.tapir._
import sttp.tapir.json.tethysjson.jsonBody
import sych.ad.common.dto.ApiErrorResponse

object StackOverflowEndpoints {
  // (apiVersion, questionId, site, authorization)
  private type questionInput = (String, Long, String, String)
  val question: Endpoint[Unit, questionInput, ApiErrorResponse, QuestionResponse, Any] =
    endpoint
      .get
      .in(path[String]("apiVersion"))
      .in("questions" / path[Long]("questionId"))
      .in(query[String]("site").default("stackoverflow"))
      .in(query[String]("key"))
      .errorOut(jsonBody[ApiErrorResponse])
      .out(jsonBody[QuestionResponse])

  // (apiVersion, questionId, site, authorization, filter, order, sort, fromdate)
  private type commentsInput = (String, Long, String, String, String, String, String, Long)
  val comments: Endpoint[Unit, commentsInput, ApiErrorResponse, Response, Any] =
    endpoint
      .get
      .in(path[String]("apiVersion"))
      .in("questions" / path[Long]("questionId") / "comments")
      .in(query[String]("site").default("stackoverflow"))
      .in(query[String]("key"))
      .in(query[String]("filter").default("withbody"))
      .in(query[String]("order").default("desc"))
      .in(query[String]("sort").default("creation"))
      .in(query[Long]("fromdate"))
      .errorOut(jsonBody[ApiErrorResponse])
      .out(jsonBody[Response])

  private type answerInput = (String, Long, String, String, String, String, String, Long)
  val answers: Endpoint[Unit, answerInput, ApiErrorResponse, Response, Any] =
    endpoint
      .get
      .in(path[String]("apiVersion"))
      .in("questions" / path[Long]("questionId") / "answers")
      .in(query[String]("site").default("stackoverflow"))
      .in(query[String]("key"))
      .in(query[String]("filter").default("withbody"))
      .in(query[String]("order").default("desc"))
      .in(query[String]("sort").default("activity"))
      .in(query[Long]("fromdate"))
      .errorOut(jsonBody[ApiErrorResponse])
      .out(jsonBody[Response])
}
