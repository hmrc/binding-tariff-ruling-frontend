/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.http.Status.FORBIDDEN
import play.api.mvc.{Request, Result, Results}
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

import scala.concurrent.Future

class AdminActionSpec extends BaseSpec {

  private val block  = mock[Request[_] => Future[Result]]
  private val config = mock[AppConfig]
  private val action = new AdminAction(config)

  "Authenticated Action" should {
    "Filter authenticated if enabled" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.adminEnabled) willReturn true

      await(action.invokeBlock(FakeRequest(), block)) shouldBe Results.Ok
    }

    "not allow authenticated action if disabled" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.adminEnabled) willReturn false

      status(await(action.invokeBlock(FakeRequest(), block))) shouldBe FORBIDDEN
    }
  }

}
