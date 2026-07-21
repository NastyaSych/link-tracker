package sych.ad.scrapper.scheduler

import cats.effect.std.Queue
import cats.effect.{IO, Resource}
import com.itv.scheduler._
import org.quartz.{CronExpression, JobKey, TriggerKey}
import sych.ad.scrapper.config.UpdaterConfig

import java.util.Properties

class Scheduler(
    updaterService: UpdatingService,
    updaterConfig: UpdaterConfig
) {

  def run: Resource[IO, Unit] = {
    val props = new Properties()
    props.setProperty("org.quartz.threadPool.threadCount", "5")
    val quartzProperties = QuartzProperties(props)
    for {
      queue      <- Resource.eval(Queue.unbounded[IO, UpdateTask])
      jobFactory <- MessageQueueJobFactory.autoAcking[IO, UpdateTask](queue)
      scheduler  <- QuartzTaskScheduler[IO, UpdateTask](quartzProperties, jobFactory)
      cronExpression = new CronExpression(updaterConfig.cronExpression)
      _ <- Resource.eval(scheduler.scheduleJob(
        JobKey.jobKey("check-all-links"),
        CheckAllLinks,
        TriggerKey.triggerKey("check-all-links-trigger"),
        CronScheduledJob(cronExpression)
      ))
      _ <- Resource.eval(
        queue.take.flatMap {
          case CheckAllLinks =>
            updaterService.updateAllAndSendUpdates().handleErrorWith { err =>
              IO.println(s"Scheduled update failed: ${err.getMessage}")
            }
        }.foreverM.start
      )
    } yield ()
  }
}

object Scheduler {
  def apply(
      updaterService: UpdatingService,
      updaterConfig: UpdaterConfig
  ): Scheduler = new Scheduler(updaterService, updaterConfig)
}
