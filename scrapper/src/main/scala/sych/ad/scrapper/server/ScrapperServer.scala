package sych.ad.scrapper.server

import cats.effect.{IO, Resource}
import sttp.tapir.server.ServerEndpoint
import sych.ad.common.server.{HttpServer, HttpServerConfig}

class ScrapperServer(
    val config: HttpServerConfig,
    scrapperController: ScrapperController
) extends HttpServer {

  override val endpoints: List[ServerEndpoint[Any, IO]] = List(
    scrapperController
  ).flatMap(_.endpoints)
}

object ScrapperServer {

  def apply(
      config: HttpServerConfig,
      scrapperController: ScrapperController
  ): ScrapperServer =
    new ScrapperServer(config, scrapperController)

  def resource(
      config: HttpServerConfig,
      scrapperController: ScrapperController
  ): Resource[IO, Unit] = {
    val server = new ScrapperServer(config, scrapperController)
    server.run
  }
}
