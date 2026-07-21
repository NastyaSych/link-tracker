package sych.ad.bot.server

import cats.effect.{IO, Resource}
import sttp.tapir.server.ServerEndpoint
import sych.ad.common.server.{HttpServer, HttpServerConfig}

class BotServer(
    val config: HttpServerConfig,
    botController: BotController
) extends HttpServer {

  override val endpoints: List[ServerEndpoint[Any, IO]] = List(
    botController
  ).flatMap(_.endpoints)
}

object BotServer {

  def apply(
      config: HttpServerConfig,
      botController: BotController
  ): BotServer = new BotServer(config, botController)

  def resource(
      config: HttpServerConfig,
      botController: BotController
  ): Resource[IO, BotServer] = Resource.pure(new BotServer(config, botController))
}
