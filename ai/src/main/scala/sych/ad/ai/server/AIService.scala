package sych.ad.ai.server

import cats.effect.IO
import org.typelevel.log4cats.LoggerFactory
import sych.ad.ai.config.AIProcessingConfig
import sych.ad.ai.kafka.AIProducer
import sych.ad.ai.messages.AIMessageSender
import sych.ad.common.dto.LinkUpdate

class AIService(
    aiMessageSender: AIMessageSender,
    producer: AIProducer,
    processingConfig: AIProcessingConfig
)(implicit L: LoggerFactory[IO]) {

  private val logger = L.getLogger

  def processLinkUpdate(update: LinkUpdate): IO[Unit] = {
    for {
      _ <- logger.info(s"Processing update ${update.id}: ${update.url}")

      maybeMessage = aiMessageSender.process(update, processingConfig)

      _ <- maybeMessage match {
        case Some((message, tgChatIds)) =>
          val processedUpdate = LinkUpdate(
            id = update.id,
            url = update.url,
            description = message,
            tgChatIds = tgChatIds
          )
          producer.produce(processedUpdate) *>
            logger.info(s"Sent update ${update.id} to Kafka")

        case None =>
          logger.info(s"Skipped update ${update.id} (filtered)")
      }
    } yield ()
  }
}
