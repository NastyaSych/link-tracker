package sych.ad.scrapper.config

import cats.effect.IO
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import sych.ad.common.server.HttpServerConfig
import sych.ad.scrapper.notification.MessageSenderMode

import scala.concurrent.duration.FiniteDuration

case class UpdateProcessingConfig(
    batchSize: Int,
    parallelThreads: Int
)

case class UpdaterConfig(
    cronExpression: String
)

case class BotClientConfig(
    baseUri: String,
    port: Int,
    timeout: FiniteDuration
)

case class GithubConfig(
    baseUri: String,
    apiVersion: String,
    apiToken: String,
    timeout: FiniteDuration
)

case class StackOverflowConfig(
    baseUri: String,
    apiVersion: String,
    apiKey: String,
    timeout: FiniteDuration
)

case class CircuitBreakerConfig(
    maxFailures: Int,
    resetTimeout: FiniteDuration
)

case class RetryConfig(
    maxRetries: Int,
    backoffDelay: FiniteDuration
//    retryableStatuses: Set[Int]
)

case class RateLimitingConfig(
    maxConcurrent: Int
)

case class HttpClientConfig(
    circuitBreaker: CircuitBreakerConfig,
    retry: RetryConfig,
    rateLimiting: RateLimitingConfig
)

case class ScrapperConfig(
    httpServer: HttpServerConfig,
    bot: BotClientConfig,
    github: GithubConfig,
    stackOverflow: StackOverflowConfig,
    updater: UpdaterConfig,
    updateProcessing: UpdateProcessingConfig,
    database: DatabaseConfig,
    linkUpdateProducer: KafkaProducerConfig,
    messageSenderMode: MessageSenderMode,
    valkey: ValkeyConfig,
    httpClient: HttpClientConfig
)

object ScrapperConfig {
  def load: IO[ScrapperConfig] =
    IO.delay(ConfigSource.default.loadOrThrow[ScrapperConfig])
}
