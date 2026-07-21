package sych.ad.scrapper.client.stackoverflow

import cats.effect.{IO, Resource}
import sttp.client3._
import sttp.model.Uri
import sych.ad.common.client.HttpClient
import sych.ad.scrapper.config.{HttpClientConfig, StackOverflowConfig}
import sych.ad.scrapper.dto.StackOverflowUpdatedInfo

import java.time.Instant

trait StackOverflowClient {
  def getLatestActivity(questionId: Long, lastChecked: Instant): IO[List[StackOverflowUpdatedInfo]]
}

class StackOverflowClientImpl(
    val backend: SttpBackend[IO, Any],
    stackOverflowConfig: StackOverflowConfig
) extends StackOverflowClient with HttpClient {
  override val baseUri: Uri = uri"${stackOverflowConfig.baseUri}"

  override def getLatestActivity(questionId: Long, lastChecked: Instant): IO[List[StackOverflowUpdatedInfo]] = {
    val fromDate = lastChecked.getEpochSecond
    for {
      question <- fromEndpoint(
        StackOverflowEndpoints.question,
        (stackOverflowConfig.apiVersion, questionId, "stackoverflow", stackOverflowConfig.apiKey)
      )
      answers <-
        fromEndpoint(
          StackOverflowEndpoints.answers,
          (
            stackOverflowConfig.apiVersion,
            questionId,
            "stackoverflow",
            stackOverflowConfig.apiKey,
            "withbody",
            "desc",
            "activity",
            fromDate
          )
        )
      comments <- fromEndpoint(
        StackOverflowEndpoints.comments,
        (
          stackOverflowConfig.apiVersion,
          questionId,
          "stackoverflow",
          stackOverflowConfig.apiKey,
          "withbody",
          "desc",
          "creation",
          fromDate
        )
      )
    } yield {
      val questionTitle = question.items.headOption.map(_.title).getOrElse("Unknown")
      val now           = Instant.now()
      val newAnswers    = answers.items
        .filter(_.createdAt.isAfter(lastChecked))
        .filter(_.createdAt.isBefore(now))

      val newComments = comments.items
        .filter(_.createdAt.isAfter(lastChecked))
        .filter(_.createdAt.isBefore(now))

      val allEvents = newAnswers ++ newComments

      allEvents.map { event =>
        StackOverflowUpdatedInfo(
          updatedAt = event.createdAt,
          title = questionTitle,
          userName = event.owner.display_name,
          preview = event.body.take(200)
        )
      }
    }
  }
}

object StackOverflowClient {
  def resource(
      stackOverflowConfig: StackOverflowConfig,
      httpClientConfig: HttpClientConfig
  ): Resource[IO, StackOverflowClient] = {
    val options              = SttpBackendOptions.Default.connectionTimeout(stackOverflowConfig.timeout)
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
      new StackOverflowClientImpl(backend, stackOverflowConfig)
    }
  }
}
