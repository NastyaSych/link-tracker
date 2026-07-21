package sych.ad.common.endpoints

import sttp.tapir._
import sttp.tapir.json.tethysjson.jsonBody
import ApiEndpoint._
import sych.ad.common.dto.{AddLinkRequest, ApiErrorResponse, LinkResponse, ListLinksResponse, RemoveLinkRequest}

object ScrapperEndpoints {

  val registerChat: Endpoint[Unit, (Long, Long), ApiErrorResponse, Unit, Any] = {
    apiEndpoint
      .post
      .in("tg-chat" / path[Long]("chatId") / path[Long]("userId"))
      .summary("Register chat")
  }

  val deleteChat: Endpoint[Unit, (Long, Long), ApiErrorResponse, Unit, Any] = {
    apiEndpoint
      .delete
      .in("tg-chat" / path[Long]("chatId") / path[Long]("userId"))
      .summary("Remove chat")
  }

  val getLinks: Endpoint[Unit, (Long, Long), ApiErrorResponse, ListLinksResponse, Any] = {
    apiEndpoint
      .get
      .in("links")
      .in(header[Long]("Tg-Chat-Id"))
      .in(header[Long]("User-Id"))
      .out(jsonBody[ListLinksResponse])
      .summary("Get the tracking list")
  }

  val addLink: Endpoint[Unit, (Long, Long, AddLinkRequest), ApiErrorResponse, LinkResponse, Any] = {
    apiEndpoint
      .summary("Add link for tracking")
      .post
      .in("links")
      .in(header[Long]("Tg-Chat-Id"))
      .in(header[Long]("User-Id"))
      .in(jsonBody[AddLinkRequest])
      .out(jsonBody[LinkResponse])
  }

  val deleteLink: Endpoint[Unit, (Long, Long, RemoveLinkRequest), ApiErrorResponse, LinkResponse, Any] = {
    apiEndpoint
      .delete
      .in("links")
      .in(header[Long]("Tg-Chat-Id"))
      .in(header[Long]("User-Id"))
      .in(jsonBody[RemoveLinkRequest])
      .out(jsonBody[LinkResponse])
      .summary("Remove link from tracking list")
  }
}
