package sych.ad.scrapper.client.github

import cats.effect.{IO, Resource}
import sttp.client3._
import sttp.model.Uri
import GithubEndpoints.IssuesInput
import sych.ad.common.client.HttpClient
import sych.ad.scrapper.config.{GithubConfig, HttpClientConfig}
import sych.ad.scrapper.dto.{GithubUpdatedInfo, Issue, PullRequest}

import java.time.Instant

trait GithubClient {

  def getLatestActivity(user: String, repo: String, since: Instant): IO[List[GithubUpdatedInfo]]
}

class GithubClientImpl(
    val backend: SttpBackend[IO, Any],
    githubConfig: GithubConfig
) extends GithubClient with HttpClient {

  override val baseUri: Uri = uri"${githubConfig.baseUri}"

  override def getLatestActivity(user: String, repo: String, since: Instant): IO[List[GithubUpdatedInfo]] = {
    val input: IssuesInput = (user, repo, s"Bearer ${githubConfig.apiToken}", "all", "updated", "desc", "10")
    fromEndpoint(GithubEndpoints.issues, input)
      .map { rawList =>
        rawList
          .filter(_.updatedAt.isAfter(since))
          .map {
            raw =>
              GithubUpdatedInfo(
                updatedAt = raw.updatedAt,
                title = raw.title,
                eventType = if (raw.pull_request.isDefined) PullRequest else Issue,
                userName = raw.user.login,
                raw.body.getOrElse("").take(200)
              )
          }
      }
  }
}

object GithubClient {
  def resource(githubConfig: GithubConfig, httpClientConfig: HttpClientConfig): Resource[IO, GithubClient] = {
    val options              = SttpBackendOptions.Default.connectionTimeout(githubConfig.timeout)
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
      new GithubClientImpl(backend, githubConfig)
    }
  }
}
