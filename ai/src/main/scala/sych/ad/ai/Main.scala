package sych.ad.ai

import cats.effect.{IO, IOApp, Resource}
import org.typelevel.log4cats.slf4j.Slf4jFactory
import sych.ad.ai.config.AIConfig
import sych.ad.ai.kafka.{AIConsumer, AIProducer}
import sych.ad.ai.messages.AIMessageSender
import sych.ad.ai.server.AIService

object Main extends IOApp.Simple {

  implicit val logging: Slf4jFactory[IO] = Slf4jFactory.create[IO]

  override def run: IO[Unit] = {
    val resources = for {
      config   <- Resource.eval(AIConfig.load)
      producer <- AIProducer.make(config.aiProducer)
      aiService = new AIService(AIMessageSender, producer, config.aiProcessing)

      _ <- AIConsumer.run(aiService, config.aiConsumer)
    } yield ()
    resources.use(_ => IO.never)
  }
}
