package sych.ad.scrapper.scheduler

import cats.effect.IO
import cats.implicits._
import fs2.Stream
import org.typelevel.log4cats.LoggerFactory
import sych.ad.scrapper.config.UpdateProcessingConfig
import sych.ad.scrapper.dto.{Link, LinkUpdatedInfo, User}
import sych.ad.scrapper.notification.MessageSender
import sych.ad.scrapper.service.ScrappingService
import sych.ad.scrapper.service.data.DataService

import java.time.Instant

trait UpdatingService {
  def updateAllAndSendUpdates(): IO[Unit]
}

class UpdatingServiceImpl(
    scrappingService: ScrappingService,
    dataService: DataService,
    messageSender: MessageSender,
    updateProcessingConfig: UpdateProcessingConfig
)(implicit L: LoggerFactory[IO]) extends UpdatingService {

  private val logger = L.getLogger

  override def updateAllAndSendUpdates(): IO[Unit] = {
    Stream
      .eval(dataService.getUsersWithLinks())
      .flatMap(usersWithLinks => Stream.emits(usersWithLinks.toList))
      .parEvalMap(updateProcessingConfig.parallelThreads) { case (user, links) =>
        updateUserLinksAndSendUpdates(user, links).handleErrorWith { err =>
          logger.error(s"Failed to update user $user: ${err.getMessage}")
        }
      }
      .compile
      .drain
  }

  private def updateLink(link: Link): IO[(Link, Boolean, List[LinkUpdatedInfo])] = {
    val lastChecked = link.updateInfo.map(_.updatedAt).getOrElse(Instant.now())
    for {
      updatesList <- scrappingService.lastUpdate(link.url, lastChecked)
      isUpdated   = updatesList.nonEmpty
      updatedLink = link.copy(updateInfo =
        if (updatesList.nonEmpty)
          Some(updatesList.maxBy(_.updatedAt))
        else
          link.updateInfo
      )
      _ <- logger.debug(s"Updater: Link ${link.url} updated: $isUpdated (${updatesList.size} events)")
    } yield (updatedLink, isUpdated, updatesList)
  }

  private def updateUserLinksAndSendUpdates(user: User, links: List[Link]): IO[Unit] = {
    for {
      _ <- logger.info(s"Starting update for user $user with ${links.length} links")

      updatedLinks <- links.parTraverse { link =>
        updateLink(link).attempt.flatMap {
          case Right((updatedLink, isUpdated, updates)) =>
            IO.pure((updatedLink, isUpdated, updates))
          case Left(err) =>
            logger.error(s"Failed to update link ${link.url} for user $user: ${err.getMessage}") *>
              IO.pure((link, false, List.empty[LinkUpdatedInfo]))
        }
      }

      updatedOnlyLinks = updatedLinks.collect {
        case (link, true, _) => link.updateInfo.map(link -> _)
      }.flatten

      _ <- dataService.patchUserLinks(user, updatedLinks.map(_._1)).handleErrorWith { err =>
        logger.error(s"Failed to patch links for user $user: ${err.getMessage}")
      }

      _ <- if (updatedOnlyLinks.nonEmpty) {
        val allUpdates = updatedLinks.flatMap { case (link, _, updates) =>
          updates.map(update => (link, update))
        }
        allUpdates.traverse { case (link, update) =>
          messageSender.formatAndSendUpdate(user, link, update)
        }.void *> logger.info(s"Sent ${allUpdates.size} updates to user ${user.userId}")
      } else {
        logger.info(s"No updates to send to user $user")
      }
    } yield ()
  }
}

object UpdatingService {
  def apply(
      scrappingService: ScrappingService,
      dataService: DataService,
      messageSender: MessageSender,
      updateProcessingConfig: UpdateProcessingConfig
  )(implicit L: LoggerFactory[IO]): UpdatingService =
    new UpdatingServiceImpl(scrappingService, dataService, messageSender, updateProcessingConfig)
}
