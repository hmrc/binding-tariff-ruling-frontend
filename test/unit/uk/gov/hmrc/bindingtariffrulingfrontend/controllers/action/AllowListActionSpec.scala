/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.{Request, Result, Results}
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.filters.AllowListFilter

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AllowListActionSpec extends BaseSpec {

  private val block  = mock[Request[_] => Future[Result]]
  private val config = mock[AppConfig]
  private val filter = new AllowListFilter(config)
  private val action = new AllowListAction(config, filter)

  "Allowed Action" should {
    "redirect unrecognised IPs" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.allowListEnabled) willReturn true
      given(config.allowListDestination) willReturn "https://gov.uk"
      given(config.allowList) willReturn Set.empty[String]

      await(action.invokeBlock(FakeRequest().withHeaders("True-Client-IP" -> "ip"), block)) shouldBe Results.Redirect(
        "https://gov.uk"
      )
    }

    "return error when no request header is set" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.allowListEnabled) willReturn true
      given(config.allowListDestination) willReturn "https://gov.uk"
      given(config.allowList) willReturn Set.empty[String]

      await(action.invokeBlock(FakeRequest(), block)) shouldBe Results.NotImplemented
    }

    "return OK when the IP is recognised" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.allowListEnabled) willReturn true
      given(config.allowList) willReturn Set("ip")

      await(action.invokeBlock(FakeRequest().withHeaders("True-Client-IP" -> "ip"), block)) shouldBe Results.Ok
    }

    "return OK when the allowlist is disabled" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.allowListEnabled) willReturn false
      given(config.allowList) willReturn Set.empty[String]

      await(action.invokeBlock(FakeRequest(), block)) shouldBe Results.Ok
    }
  }

}
