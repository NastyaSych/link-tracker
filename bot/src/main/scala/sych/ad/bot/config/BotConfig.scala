package sych.ad.bot.config

import cats.effect.IO
import pureconfig._
import pureconfig.generic.auto._
import sych.ad.common.server.HttpServerConfig

import scala.concurrent.duration.FiniteDuration

case class TgBotConfig(
    baseUrl: String,
    token: String,
    botName: String
)

case class ScrapperClientConfig(
    baseUri: String,
    port: Int,
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

case class BotConfig(
    tgBot: TgBotConfig,
    httpServer: HttpServerConfig,
    scrapper: ScrapperClientConfig,
    linkUpdateConsumer: KafkaConsumerConfig,
    httpClient: HttpClientConfig
)

object BotConfig {
  def load: IO[BotConfig] = IO.delay(ConfigSource.default.loadOrThrow[BotConfig])
}
