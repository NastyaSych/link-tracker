package sych.ad.common.client

import cats.effect.IO
import io.chrisdavenport.circuit.CircuitBreaker
import sttp.capabilities.Effect
import sttp.client3._

class CircuitBreakerBackend[P](
    delegate: SttpBackend[IO, P],
    cb: CircuitBreaker[IO]
) extends DelegateSttpBackend[IO, P](delegate) {

  override def send[T, R >: P with Effect[IO]](request: Request[T, R]): IO[Response[T]] = {
    cb.protect(delegate.send(request))
  }
}
