package sych.ad.scrapper

import cats.effect.{IO, IOApp, Resource}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import sych.ad.scrapper.client.bot.BotClient
import sych.ad.scrapper.client.github.GithubClient
import sych.ad.scrapper.client.stackoverflow.StackOverflowClient
import sych.ad.scrapper.config.ScrapperConfig
import sych.ad.scrapper.database.{DatabaseTransactor, LiquibaseMigration, SlickModule}
import sych.ad.scrapper.kafka.LinkUpdateProducer
import sych.ad.scrapper.notification.{HttpMessageSender, KafkaMessageSender, MessageSenderMode}
import sych.ad.scrapper.scheduler.{Scheduler, UpdatingService}
import sych.ad.scrapper.server.{ScrapperController, ScrapperServer}
import sych.ad.scrapper.service.ScrappingService
import sych.ad.scrapper.service.data.{DataService, LinksRepository}
import sych.ad.scrapper.service.data.inmemory.InMemoryRepository
import sych.ad.scrapper.service.data.orm.SlickRepository
import sych.ad.scrapper.service.data.sql.PostgresRepository
import sych.ad.scrapper.valkey.GetLinksCache

import scala.concurrent.ExecutionContext

object Main extends IOApp.Simple {
  override def run: IO[Unit] = {
    implicit val ec: ExecutionContext       = ExecutionContext.global
    implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val logger                              = logging.getLogger
    val appResource                         = for {
      config <- Resource.eval(ScrapperConfig.load)
      _      <- Resource.eval(logger.info(s"Config loaded, access type: ${config.database.accessType}"))

      linksRepository <- config.database.accessType match {
        case "inmemory" =>
          for {
            _    <- Resource.eval(logger.info("Using InMemory repository"))
            repo <- Resource.pure[IO, LinksRepository](new InMemoryRepository())
          } yield repo
        case "sql" =>
          for {
            _    <- Resource.eval(logger.info("Using SQL (Postgre) repository"))
            db   <- DatabaseTransactor.resource(config.database)
            _    <- Resource.eval(LiquibaseMigration.run(db)(logging))
            repo <- Resource.pure[IO, LinksRepository](new PostgresRepository(db.transactor))
          } yield repo
        case "orm" =>
          for {
            _    <- Resource.eval(logger.info("Using ORM (Slick) repository"))
            db   <- SlickModule.resource(config.database)
            repo <- Resource.pure[IO, LinksRepository](new SlickRepository(db))
          } yield repo
        case other =>
          for {
            _    <- Resource.eval(logger.error(s"Unknown access type: $other, using InMemory repository"))
            repo <- Resource.pure[IO, LinksRepository](new InMemoryRepository())
          } yield repo
      }

      githubClient   <- GithubClient.resource(config.github, config.httpClient)
      stackClient    <- StackOverflowClient.resource(config.stackOverflow, config.httpClient)
      botClient      <- BotClient.resource(config.bot, config.httpClient)
      updateProducer <- LinkUpdateProducer.make(config.linkUpdateProducer)
      messageSender = config.messageSenderMode match {
        case MessageSenderMode.Kafka => KafkaMessageSender(updateProducer)
        case MessageSenderMode.Http  => HttpMessageSender(botClient, fallback = KafkaMessageSender(updateProducer))
      }
      scrappingService = ScrappingService(githubClient, stackClient)
      dataService      = DataService(linksRepository, scrappingService)(logging)

      updaterService = UpdatingService(scrappingService, dataService, messageSender, config.updateProcessing)(logging)

      scheduler <- Scheduler(updaterService, config.updater).run

      getLinksCache <- GetLinksCache.make(config.valkey)

      controller = new ScrapperController(dataService, getLinksCache)(logging)
      _      <- Resource.eval(logger.info(s"Starting server on port ${config.httpServer.port}"))
      server <- ScrapperServer.resource(config.httpServer, controller)
    } yield (scheduler, server)

    appResource.use(_ => IO.never)
  }
}
