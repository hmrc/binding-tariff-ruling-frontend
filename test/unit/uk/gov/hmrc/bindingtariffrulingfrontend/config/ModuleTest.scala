
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

package uk.gov.hmrc.bindingtariffrulingfrontend.config

import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.PlayRunners
import uk.gov.hmrc.bindingtariffrulingfrontend.UnitSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.scheduler.{BackendScheduler, ScheduledJobs}

class ModuleTest extends UnitSpec with BeforeAndAfterEach with PlayRunners {

  private def app(conf: (String, Any)*): Application =
    new GuiceApplicationBuilder()
      .bindings(new Module)
      .configure(conf: _*)
      .build()

  "Module 'bind" should {
    "Bind all scheduled jobs" in {
      val application: Application = app()
      running(application) {
        application.injector.instanceOf[ScheduledJobs].jobs shouldBe Set(
          application.injector.instanceOf[BackendScheduler],
          application.injector.instanceOf[ScheduledJobProvider]
        )
      }
    }
  }

}
