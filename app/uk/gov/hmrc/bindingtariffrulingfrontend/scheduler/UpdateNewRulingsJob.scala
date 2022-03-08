/*
 * Copyright 2022 HM Revenue & Customs
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

import com.google.inject.Inject
import org.quartz.JobExecutionContext
import play.api.Logging
import uk.gov.hmrc.bindingtariffrulingfrontend.workers.RulingsWorker
import uk.gov.hmrc.http.HeaderCarrier

import java.time.temporal.ChronoUnit
import java.time.{Duration, Instant, LocalTime}
import javax.inject.Singleton

//scalastyle:off magic.number
@Singleton
class UpdateNewRulingsJob @Inject() (rulingsWorker: RulingsWorker) extends ScheduledJob with Logging {
  private implicit val headers: HeaderCarrier = HeaderCarrier()

  override def jobName: String = "Update new rulings"

  override def schedule: Either[Duration, LocalTime] = Right(LocalTime.of(2, 0))

  override def execute(context: JobExecutionContext): Unit = {
    logger.info(s"Backend scheduler for updateNewRulingsJob started at ${Instant.now}")

    rulingsWorker.updateNewRulings(Instant.now().minus(12, ChronoUnit.HOURS))
  }
}
