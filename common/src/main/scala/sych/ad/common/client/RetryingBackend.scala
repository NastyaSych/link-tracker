// copypast with some changes from https://sttp.softwaremill.com/en/v3.0.0/backends/wrappers/custom.html
package sych.ad.common.client

import cats.effect.IO
import sttp.capabilities.Effect
import sttp.client3._

import scala.concurrent.duration.FiniteDuration

class RetryingBackend[P](
    delegate: SttpBackend[IO, P],
    shouldRetry: RetryWhen,
    maxRetries: Int,
    backoffDelay: FiniteDuration
) extends DelegateSttpBackend[IO, P](delegate) {

  override def send[T, R >: P with Effect[IO]](request: Request[T, R]): IO[Response[T]] = {
    sendWithRetryCounter(request, 0)
  }

  private def sendWithRetryCounter[T, R >: P with Effect[IO]](
      request: Request[T, R],
      retries: Int
  ): IO[Response[T]] = {
    delegate.send(request).handleErrorWith {
      case t if shouldRetry(request, Left(t)) && retries < maxRetries =>
        IO.sleep(backoffDelay) *> sendWithRetryCounter(request, retries + 1)
      case t => IO.raiseError(t)
    }.flatMap { resp =>
      if (shouldRetry(request, Right(resp)) && retries < maxRetries) {
        IO.sleep(backoffDelay) *> sendWithRetryCounter(request, retries + 1)
      } else {
        IO.pure(resp)
      }
    }
  }
}
