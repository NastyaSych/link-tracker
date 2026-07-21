package sych.ad.common.server

import cats.effect.{IO, Resource}
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.ExecutionContext.global

trait HttpServer {
  def config: HttpServerConfig

  def endpoints: List[ServerEndpoint[Any, IO]]

  def name: String = getClass.getSimpleName

  def docsEndpoints: List[ServerEndpoint[Any, IO]] =
    SwaggerInterpreter()
      .fromServerEndpoints[IO](endpoints, name, config.version)

  def allRoutes: HttpRoutes[IO] = {
    Http4sServerInterpreter[IO]().toRoutes(endpoints ++ docsEndpoints)
  }

  def run: Resource[IO, Unit] = {
    for {
      _ <- BlazeServerBuilder[IO]
        .withExecutionContext(global)
        .bindHttp(config.port, "0.0.0.0")
        .withHttpApp(Router("/" -> allRoutes).orNotFound)
        .resource
    } yield ()
  }
}
