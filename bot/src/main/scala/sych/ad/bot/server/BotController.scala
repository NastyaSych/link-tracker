package sych.ad.bot.server

import cats.effect.IO
import sttp.tapir.server.ServerEndpoint
import sych.ad.common.controller.Controller
import sych.ad.common.dto.{ApiErrorResponse, LinkUpdate}
import sych.ad.common.endpoints.BotEndpoints
import sych.ad.common.dto.ApiErrorResponse.IOWithApiError

class BotController(
    botService: BotService
) extends Controller {

  private val updates: ServerEndpoint.Full[Unit, Unit, LinkUpdate, ApiErrorResponse, Unit, Any, IO] =
    BotEndpoints.updates
      .serverLogic { linkUpdate =>
        botService.handleLinkUpdate(linkUpdate).toApiError
      }

  override val endpoints: List[ServerEndpoint[Any, IO]] = List(updates)
}

object BotController {
  def apply(botService: BotService): BotController = new BotController(botService)
}
