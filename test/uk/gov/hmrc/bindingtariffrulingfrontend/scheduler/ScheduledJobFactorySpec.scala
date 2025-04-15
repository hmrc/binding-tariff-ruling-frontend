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

import org.mockito.Mockito.{mock, verify, when}
import org.quartz.{Job, JobDetail, Scheduler}
import org.quartz.spi.TriggerFiredBundle
import play.api.inject.Injector
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class ScheduledJobFactorySpec extends BaseSpec {
  "ScheduledJobFactory" should {
    "create new jobs using the injector" in {
      val mockInjector  = mock(classOf[Injector])
      val mockScheduler = mock(classOf[Scheduler])
      val mockBundle    = mock(classOf[TriggerFiredBundle])
      val mockJobDetail = mock(classOf[JobDetail])

      val jobClass = classOf[UpdateNewRulingsJob]

      when(mockBundle.getJobDetail).thenReturn(mockJobDetail)
      when(mockJobDetail.getJobClass).thenReturn(jobClass)

      val mockJob = mock(classOf[UpdateNewRulingsJob])
      when(mockInjector.instanceOf(jobClass)).thenReturn(mockJob)

      val factory = new ScheduledJobFactory(mockInjector)

      val result = factory.newJob(mockBundle, mockScheduler)

      result shouldBe mockJob

      verify(mockBundle).getJobDetail
      verify(mockJobDetail).getJobClass
      verify(mockInjector).instanceOf(jobClass)
    }
  }
}
