package sych.ad.common.endpoints

import sttp.tapir._
import sttp.tapir.json.tethysjson.jsonBody
import ApiEndpoint._
import sych.ad.common.dto.{ApiErrorResponse, LinkUpdate}

object BotEndpoints {

  val updates: Endpoint[Unit, LinkUpdate, ApiErrorResponse, Unit, Any] =
    apiEndpoint
      .post
      .in("updates")
      .in(jsonBody[LinkUpdate])
      .summary("Send message")

}
