package sych.ad.scrapper.database

import cats.effect.IO
import doobie._
import doobie.implicits._
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.typelevel.log4cats.LoggerFactory

object LiquibaseMigration {
  def run(
      xa: DatabaseTransactor,
      changelog: String = "migrations/changelog.xml"
  )(implicit L: LoggerFactory[IO]): IO[Unit] = {
    val logger = L.getLogger
    for {
      _ <- logger.info("Starting liquibase migration...")
      _ <- FC.raw { conn =>
        val resourceAccessor = new ClassLoaderResourceAccessor()
        val database         = DatabaseFactory.getInstance()
          .findCorrectDatabaseImplementation(new JdbcConnection(conn))
        val liquibase = new Liquibase(changelog, resourceAccessor, database)
        liquibase.update()
      }.transact(xa.transactor)
      _ <- logger.info("Liquibase migration completed successfully")
    } yield ()
  }
}
