/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers

import akka.stream.Materializer
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.{WhitelistDisabled, WhitelistEnabled, WhitelistedAction}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService

import scala.concurrent.Future


class SearchControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private implicit val mat: Materializer = fakeApplication.materializer
  private val rulingService = mock[RulingService]

  private def controller(whitelist: WhitelistedAction = WhitelistDisabled()) = new SearchController(rulingService, whitelist, messageApi, appConfig)

  "GET /" should {
    "return 200 without form" in {
      val result = await(controller().get(query = None, page = 1)(getRequestWithCSRF()))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("search-heading")

      verifyZeroInteractions(rulingService)
    }

    "return 200 with form" in {
      given(rulingService.get(any[SimpleSearch])) willReturn Future.successful(Paged.empty[Ruling])

      val result = await(controller().get(query = Some("query"), page = 1)(getRequestWithCSRF("/?query=query&page=1")))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("search-heading")

      verify(rulingService).get(SimpleSearch("query", 1))
    }

    "return 200 with form errors" in {
      val result = await(controller().get(query = Some(""), page = 1)(getRequestWithCSRF()))

      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("search-heading")

      verifyZeroInteractions(rulingService)
    }

    "return 403 when whitelisted" in {
      val result = await(controller(whitelist = WhitelistEnabled()).get(query = None, page = 1)(getRequestWithCSRF()))

      status(result) shouldBe Status.FORBIDDEN
    }
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(rulingService)
  }
}