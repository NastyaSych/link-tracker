package sych.ad.scrapper.service

import cats.effect.IO
import sych.ad.common.util.LinkParser
import sych.ad.scrapper.client.github.GithubClient
import sych.ad.scrapper.client.stackoverflow.StackOverflowClient
import sych.ad.scrapper.dto.LinkUpdatedInfo

import java.time.Instant

trait ScrappingService {
  def lastUpdate(link: String, lastChecked: Instant): IO[List[LinkUpdatedInfo]]
}

class ScrappingServiceImpl(
    githubClient: GithubClient,
    stackOverflowClient: StackOverflowClient
) extends ScrappingService {
  override def lastUpdate(link: String, lastChecked: Instant): IO[List[LinkUpdatedInfo]] = {
    LinkParser.parseGithubLink(link) match {
      case Some((user, repo)) =>
        githubClient.getLatestActivity(user, repo, lastChecked)
      case None =>
        LinkParser.parseStackOverflowLink(link) match {
          case Some(questionId) =>
            stackOverflowClient.getLatestActivity(questionId, lastChecked)
          case None =>
            IO.pure(List.empty)
        }
    }
  }
}

object ScrappingService {

  def apply(githubClient: GithubClient, stackOverflowClient: StackOverflowClient): ScrappingService =
    new ScrappingServiceImpl(githubClient, stackOverflowClient)

  def resource(githubClient: GithubClient, stackOverflowClient: StackOverflowClient): IO[ScrappingService] =
    IO.pure(new ScrappingServiceImpl(githubClient, stackOverflowClient))
}
