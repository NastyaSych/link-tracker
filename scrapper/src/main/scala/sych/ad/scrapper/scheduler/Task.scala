package sych.ad.scrapper.scheduler

import com.itv.scheduler.extruder.semiauto._
import com.itv.scheduler.{JobDataEncoder, JobDecoder}

sealed trait UpdateTask
case object CheckAllLinks extends UpdateTask

object UpdateTask {
  implicit val encoder: JobDataEncoder[UpdateTask] = deriveJobEncoder[UpdateTask]
  implicit val decoder: JobDecoder[UpdateTask]     = deriveJobDecoder[UpdateTask]
}
