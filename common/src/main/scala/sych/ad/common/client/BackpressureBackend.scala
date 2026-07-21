package sych.ad.common.client

import cats.effect.IO
import cats.effect.std.Backpressure
import sttp.capabilities.Effect
import sttp.client3._

class BackpressureBackend[P](
    delegate: SttpBackend[IO, P],
    backpressure: Backpressure[IO]
) extends DelegateSttpBackend[IO, P](delegate) {

  override def send[T, R >: P with Effect[IO]](request: Request[T, R]): IO[Response[T]] = {
    backpressure.metered(delegate.send(request)).flatMap {
      case Some(resp) => IO.pure(resp)
      case None       => IO.raiseError(new RuntimeException("Request rejected due to backpressure limit exceeded"))
    }
  }
}
