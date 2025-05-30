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

import org.apache.pekko.Done
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{doReturn, mock, verify}
import org.quartz.JobExecutionContext
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.workers.RulingsWorker
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Instant, LocalTime}
import java.time.temporal.ChronoUnit
import scala.concurrent.Future

class UpdateCanceledRulingsJobSpec extends BaseSpec {

  "UpdateCanceledRulingsJob" should {
    "execute the updateCancelledRulings task" in {
      val mockRulingsWorker = mock(classOf[RulingsWorker])

      val instantCaptor = ArgumentCaptor.forClass(classOf[Instant])
      val hcCaptor      = ArgumentCaptor.forClass(classOf[HeaderCarrier])

      doReturn(Future.successful(Done))
        .when(mockRulingsWorker)
        .updateCancelledRulings(instantCaptor.capture())(hcCaptor.capture())

      val job = new UpdateCanceledRulingsJob(mockRulingsWorker)

      val mockContext = mock(classOf[JobExecutionContext])

      job.execute(mockContext)

      verify(mockRulingsWorker).updateCancelledRulings(instantCaptor.capture())(hcCaptor.capture())

      val capturedInstant = instantCaptor.getValue
      val twelveHoursAgo  = Instant.now().minus(12, ChronoUnit.HOURS)

      val differenceInSeconds = Math.abs(capturedInstant.getEpochSecond - twelveHoursAgo.getEpochSecond)
      differenceInSeconds should be < 10L
    }

    "return the correct job name" in {
      val mockRulingsWorker = mock(classOf[RulingsWorker])
      val job               = new UpdateCanceledRulingsJob(mockRulingsWorker)

      job.jobName shouldBe "Update canceled rulings"
    }

    "schedule the job at 3 AM" in {
      val mockRulingsWorker = mock(classOf[RulingsWorker])
      val job               = new UpdateCanceledRulingsJob(mockRulingsWorker)

      job.schedule shouldBe LocalTime.of(3, 0)
    }
  }
}
