package sych.ad.common.endpoints

import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.{Endpoint, endpoint}
import sych.ad.common.dto.ApiErrorResponse

object ApiEndpoint {
  val apiEndpoint: Endpoint[Unit, Unit, ApiErrorResponse, Unit, Any] = endpoint.errorOut(jsonBody[ApiErrorResponse])
}
