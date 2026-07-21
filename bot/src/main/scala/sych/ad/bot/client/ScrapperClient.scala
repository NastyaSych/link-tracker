package sych.ad.bot.client

import cats.effect.{IO, Resource}
import sttp.client3._
import sttp.model.Uri
import sych.ad.bot.config.{HttpClientConfig, ScrapperClientConfig}
import sych.ad.common.client.HttpClient
import sych.ad.common.dto.{AddLinkRequest, LinkResponse, ListLinksResponse, RemoveLinkRequest}
import sych.ad.common.endpoints.ScrapperEndpoints

trait ScrapperClient {
  def registerChat(chatId: Long, userId: Long): IO[Unit]

  def deleteChat(chatId: Long, userId: Long): IO[Unit]

  def getLinks(chatId: Long, userId: Long): IO[ListLinksResponse]

  def addLink(chatId: Long, userId: Long, request: AddLinkRequest): IO[LinkResponse]

  def deleteLink(chatId: Long, userId: Long, request: RemoveLinkRequest): IO[LinkResponse]
}

class ScrapperClientImpl(
    val backend: SttpBackend[IO, Any],
    scrapperClientConfig: ScrapperClientConfig
) extends ScrapperClient
    with HttpClient {
  override val baseUri: Uri = uri"${scrapperClientConfig.baseUri}:${scrapperClientConfig.port}"

  override def registerChat(chatId: Long, userId: Long): IO[Unit] =
    fromEndpoint(ScrapperEndpoints.registerChat, (chatId, userId))

  override def deleteChat(chatId: Long, userId: Long): IO[Unit] =
    fromEndpoint(ScrapperEndpoints.deleteChat, (chatId, userId))

  override def getLinks(chatId: Long, userId: Long): IO[ListLinksResponse] =
    fromEndpoint(ScrapperEndpoints.getLinks, (chatId, userId))

  override def addLink(chatId: Long, userId: Long, request: AddLinkRequest): IO[LinkResponse] =
    fromEndpoint(ScrapperEndpoints.addLink, (chatId, userId, request))

  override def deleteLink(chatId: Long, userId: Long, request: RemoveLinkRequest): IO[LinkResponse] =
    fromEndpoint(ScrapperEndpoints.deleteLink, (chatId, userId, request))
}

object ScrapperClient {

  def resource(
      scrapperConfig: ScrapperClientConfig,
      httpClientConfig: HttpClientConfig
  ): Resource[IO, ScrapperClient] = {
    val options = SttpBackendOptions.Default.connectionTimeout(
      scrapperConfig.timeout
    )
    val circuitBreakerParams = (
      httpClientConfig.circuitBreaker.maxFailures,
      httpClientConfig.circuitBreaker.resetTimeout
    )
    val retryParams = (
      httpClientConfig.retry.maxRetries,
      httpClientConfig.retry.backoffDelay
    )
    val rateLimitingParams = httpClientConfig.rateLimiting.maxConcurrent

    HttpClient.resource(options, circuitBreakerParams, retryParams, rateLimitingParams).map { backend =>
      new ScrapperClientImpl(backend, scrapperConfig)
    }
  }
}
