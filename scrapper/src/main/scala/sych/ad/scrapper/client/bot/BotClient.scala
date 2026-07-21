package sych.ad.scrapper.client.bot

import cats.effect.{IO, Resource}
import sttp.client3._
import sttp.model.Uri
import sych.ad.common.client.HttpClient
import sych.ad.common.dto.LinkUpdate
import sych.ad.common.endpoints.BotEndpoints
import sych.ad.scrapper.config.{BotClientConfig, HttpClientConfig}

class BotClient(
    val backend: SttpBackend[IO, Any],
    botClientConfig: BotClientConfig
) extends HttpClient {
  override val baseUri: Uri = uri"${botClientConfig.baseUri}:${botClientConfig.port}"

  def sendLinkUpdate(linkUpdate: LinkUpdate): IO[Unit] =
    fromEndpoint(BotEndpoints.updates, linkUpdate)
}

object BotClient {

  def resource(botConfig: BotClientConfig, httpClientConfig: HttpClientConfig): Resource[IO, BotClient] = {
    val options              = SttpBackendOptions.Default.connectionTimeout(botConfig.timeout)
    val circuitBreakerParams = (
      httpClientConfig.circuitBreaker.maxFailures,
      httpClientConfig.circuitBreaker.resetTimeout
    )
    val retryParams = (
      httpClientConfig.retry.maxRetries,
      httpClientConfig.retry.backoffDelay
    )
    val rateLimitingParams = httpClientConfig.rateLimiting.maxConcurrent
    HttpClient.resource(options, circuitBreakerParams, retryParams, rateLimitingParams).map(backend =>
      new BotClient(backend, botConfig)
    )
  }
}
