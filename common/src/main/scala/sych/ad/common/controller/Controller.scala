package sych.ad.common.controller

import cats.effect.IO
import sttp.tapir.server.ServerEndpoint

trait Controller {
  val endpoints: List[ServerEndpoint[Any, IO]]
}
