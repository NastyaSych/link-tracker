package sych.ad.scrapper.notification

import cats.effect.IO
import sych.ad.common.dto.LinkUpdate
import sych.ad.scrapper.dto.{GithubUpdatedInfo, Link, LinkUpdatedInfo, PullRequest, StackOverflowUpdatedInfo, User}

import java.time.Instant

trait MessageSender {
  def sendUpdate(linkUpdate: LinkUpdate): IO[Unit]

  def formatAndSendUpdate(user: User, link: Link, updateInfo: LinkUpdatedInfo): IO[Unit] = {
    val message    = MessageSender.formatUpdateInfo(updateInfo)
    val linkUpdate = LinkUpdate(link.id, link.url, message, List(user.tgChatId))
    sendUpdate(linkUpdate)
  }
}

object MessageSender {

  private def formatUpdateInfo(updateInfo: LinkUpdatedInfo): String = {
    updateInfo match {
      case g: GithubUpdatedInfo =>
        val typeText = if (g.eventType == PullRequest) "PR" else "Issue"
        s"""GitHub $typeText!
           |
           |Title: ${g.title}
           |Author: ${g.userName}
           |Time: ${formatTime(g.updatedAt)} (MSK)
           |Description: ${g.preview}...""".stripMargin

      case s: StackOverflowUpdatedInfo =>
        s"""StackOverflow update!
           |
           |Question: ${s.title}
           |Author: ${s.userName}
           |Time: ${formatTime(s.updatedAt)}
           |Answer preview: ${s.preview}...""".stripMargin
      case _ => "Nothing to update"
    }
  }

  private def formatTime(instant: Instant): String = {
    java.time.format.DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss")
      .withZone(java.time.ZoneId.of("Europe/Moscow"))
      .format(instant)
  }
}
