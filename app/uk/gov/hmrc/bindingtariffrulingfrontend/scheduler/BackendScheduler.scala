/*
 * Copyright 2025 HM Revenue & Customs
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

import org.quartz.CronScheduleBuilder.dailyAtHourAndMinute
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.quartz.{JobDetail, Scheduler, Trigger}
import play.api.Logging
import play.api.inject.ApplicationLifecycle

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class BackendScheduler @Inject() (
  lifecycle: ApplicationLifecycle,
  scheduledJobs: ScheduledJobs,
  scheduledJobFactory: ScheduledJobFactory
) extends Logging {

  private def jobDetail(job: ScheduledJob): JobDetail =
    newJob(job.getClass).withIdentity(job.jobName).build

  private def jobTrigger(job: ScheduledJob, jobDetails: JobDetail): Trigger =
    newTrigger()
      .withIdentity(s"${job.jobName} trigger")
      .startNow()
      .withSchedule(
        dailyAtHourAndMinute(job.schedule.getHour, job.schedule.getMinute)
      )
      .forJob(jobDetails)
      .build()

  lazy val quartz: Scheduler = StdSchedulerFactory.getDefaultScheduler
  quartz.setJobFactory(scheduledJobFactory)
  scheduledJobs.jobs.foreach { job =>
    val details = jobDetail(job)
    val trigger = jobTrigger(job, details)
    quartz.scheduleJob(details, trigger)
  }

  quartz.start()

  lifecycle.addStopHook(() => Future.successful(quartz.shutdown()))

}
