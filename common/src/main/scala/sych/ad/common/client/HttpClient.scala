package sych.ad.common.client

import cats.effect.std.Backpressure
import cats.effect.{IO, Resource}
import io.chrisdavenport.circuit.CircuitBreaker
import sttp.capabilities
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.client3.{Request, Response, RetryWhen, SttpBackend, SttpBackendOptions}
import sttp.model.Uri
import sttp.tapir.Endpoint
import sttp.tapir.client.sttp.SttpClientInterpreter

import scala.concurrent.duration.FiniteDuration

trait HttpClient {
  def backend: SttpBackend[IO, Any]
  def baseUri: Uri

  private def send[Res](request: Request[Res, Any]): IO[Response[Res]] = {
    request.send(backend)
  }

  def fromEndpoint[I, E, O](
      endpoint: Endpoint[Unit, I, E, O, Any],
      input: I
  ): IO[O] = {
    val request = SttpClientInterpreter()
      .toRequestThrowErrors(endpoint, Some(baseUri))
      .apply(input)

    send(request).map(_.body)
  }
}

object HttpClient {
  def resource(
      options: SttpBackendOptions,
      circuitBreakerParams: (Int, FiniteDuration),
      retryParams: (Int, FiniteDuration),
      rateLimitingParams: Int
  ): Resource[IO, SttpBackend[IO, capabilities.WebSockets]] =
    for {
      basicBackend <- HttpClientCatsBackend.resource[IO](options = options)
      (maxRetries, backoffDelay)  = retryParams
      backendWithRetry            = new RetryingBackend(basicBackend, RetryWhen.Default, maxRetries, backoffDelay)
      (maxFailures, resetTimeout) = circuitBreakerParams
      cb <- makeCircuitBreaker(maxFailures, resetTimeout)
      backendWithCb = new CircuitBreakerBackend(backendWithRetry, cb)
      backpressure <- makeBackpressure(rateLimitingParams)
      backendWithBackpressure = new BackpressureBackend(backendWithCb, backpressure)
    } yield backendWithBackpressure

  private def makeCircuitBreaker(maxFailures: Int, resetTimeout: FiniteDuration): Resource[IO, CircuitBreaker[IO]] = {
    CircuitBreaker.default[IO](maxFailures, resetTimeout)
      .withOnOpen(IO.println("CircuitBreaker is open"))
      .withOnHalfOpen(IO.println("CircuitBreaker is half-open"))
      .withOnClosed(IO.println("CircuitBreaker is closed"))
      .build
  }.toResource

  private def makeBackpressure(maxConcurrent: Int): Resource[IO, Backpressure[IO]] =
    Resource.eval(Backpressure[IO](Backpressure.Strategy.Lossy, maxConcurrent))

}
