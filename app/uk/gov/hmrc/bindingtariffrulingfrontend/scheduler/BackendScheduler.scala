/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.bindingtariffrulingfrontend.scheduler

import org.quartz.JobBuilder.newJob
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{Job, JobDetail, JobExecutionContext}
import play.api.Logger.logger
import play.api.inject.ApplicationLifecycle

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class BackendScheduler @Inject() (lifecycle: ApplicationLifecycle) {
  lazy val quartz = StdSchedulerFactory.getDefaultScheduler

  val jobId = UUID.randomUUID().toString

  val job: JobDetail = newJob(classOf[Job]).withIdentity("job1", "group1").build

  val trigger = newTrigger()
    .withIdentity("trigger3", "group1")
    //    .withSchedule(dailyAtHourAndMinute(15, 0)) // fire every day at 15:
    .startNow()
    .withSchedule(
      simpleSchedule()
        .withIntervalInSeconds(30)
        .repeatForever()
    )
    .forJob(job)
    .build()

  quartz.scheduleJob(job, trigger)
  lifecycle.addStopHook(() => Future.successful(quartz.shutdown()))

  val exampleJob = new Job {
    override def execute(context: JobExecutionContext): Unit =
      logger.info(s"Backend scheduler started at${Instant.now}")

  }
}
