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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.{AllowListDisabled, AllowListEnabled, AllowedAction}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService

import scala.concurrent.Future

class SearchControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val rulingService = mock[RulingService]

  private def controller(allowlist: AllowedAction = AllowListDisabled()) =
    new SearchController(rulingService, allowlist, mcc, realConfig)

  "GET /" should {
    "return 200 without form" in {
      given(rulingService.get(any[SimpleSearch])) willReturn Future.successful(Paged.empty[Ruling])

      val result = await(controller().get(query = None, imagesOnly = false, page = 1)(getRequestWithCSRF()))

      status(result)                                                   shouldBe Status.OK
      contentType(result)                                              shouldBe Some("text/html")
      charset(result)                                                  shouldBe Some("utf-8")
      bodyOf(result)                                                   should include("search-heading")
      asDocument(bodyOf(result)).getElementById("search-heading").text shouldBe messageApi("search.heading")

      verify(rulingService).get(SimpleSearch(None, imagesOnly = false, 1))
    }

    "return 200 with form" in {
      given(rulingService.get(any[SimpleSearch])) willReturn Future.successful(Paged.empty[Ruling])

      val result = await(
        controller()
          .get(query = Some("query"), imagesOnly = false, page = 1)(getRequestWithCSRF("/?query=query&page=1"))
      )

      status(result)                                                   shouldBe Status.OK
      contentType(result)                                              shouldBe Some("text/html")
      charset(result)                                                  shouldBe Some("utf-8")
      bodyOf(result)                                                   should include("search-heading")
      asDocument(bodyOf(result)).getElementById("search-heading").text shouldBe messageApi("search.heading")

      verify(rulingService).get(SimpleSearch(Some("query"), imagesOnly = false, 1))
    }

    "return 200 without form errors" in {
      given(rulingService.get(any[SimpleSearch])) willReturn Future.successful(Paged.empty[Ruling])

      val result = await(controller().get(query = Some(""), imagesOnly = false, page = 1)(getRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)      should include("search-heading")

      verify(rulingService).get(SimpleSearch(None, imagesOnly = false, 1))
    }

    "return 403 when disallowed" in {
      val result = await(
        controller(allowlist = AllowListEnabled()).get(query = None, imagesOnly = false, page = 1)(getRequestWithCSRF())
      )

      status(result) shouldBe Status.FORBIDDEN
    }
  }

  def asDocument(html: String): Document = Jsoup.parse(html)

  override protected def afterEach(): Unit = {
    super.afterEach()
    Mockito.reset(rulingService)
  }

}
