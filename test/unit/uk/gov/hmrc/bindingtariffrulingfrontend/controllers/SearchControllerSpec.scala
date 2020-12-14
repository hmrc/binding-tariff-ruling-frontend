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
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.{AllowListAction, AllowListDisabled, AllowListEnabled}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.filters.RateLimitFilter
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.{FileStoreService, RulingService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

class SearchControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val appConfig        = mock[AppConfig]
  private val rulingService    = mock[RulingService]
  private val fileStoreService = mock[FileStoreService]
  private val rateLimit        = new RateLimitFilter(appConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(rulingService)
    reset(fileStoreService)
  }

  private def asDocument(html: String): Document = Jsoup.parse(html)

  private def controller(allowlist: AllowListAction = AllowListDisabled()) =
    new SearchController(rulingService, fileStoreService, allowlist, rateLimit, mcc, realConfig)

  "GET /" should {
    "return 200 with a valid query" in {
      given(rulingService.get(any[SimpleSearch])) willReturn Future.successful(Paged.empty[Ruling])
      given(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .willReturn(Future.successful(Map.empty[String, FileMetadata]))

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
      verify(fileStoreService).get(refEq(Paged.empty[Ruling]))(any[HeaderCarrier])
    }

    "return 200 with no search query" in {
      val result = await(controller().get(query = None, imagesOnly = false, page = 1)(getRequestWithCSRF()))

      status(result)                                                   shouldBe Status.OK
      contentType(result)                                              shouldBe Some("text/html")
      charset(result)                                                  shouldBe Some("utf-8")
      bodyOf(result)                                                   should include("search-heading")
      asDocument(bodyOf(result)).getElementById("search-heading").text shouldBe messageApi("search.heading")

      verifyZeroInteractions(rulingService)
      verifyZeroInteractions(fileStoreService)
    }

    "return 200 with an empty search query" in {
      val result = await(controller().get(query = Some(""), imagesOnly = false, page = 1)(getRequestWithCSRF()))

      status(result)                                                   shouldBe Status.OK
      contentType(result)                                              shouldBe Some("text/html")
      charset(result)                                                  shouldBe Some("utf-8")
      bodyOf(result)                                                   should include("search-heading")
      asDocument(bodyOf(result)).getElementById("search-heading").text shouldBe messageApi("search.heading")

      verifyZeroInteractions(rulingService)
      verifyZeroInteractions(fileStoreService)
    }

    "return 400 when form is filled incorrectly" in {
      val result = await(
        controller()
          .get(query = Some("query"), imagesOnly = false, page = 1)(getRequestWithCSRF("/?query=query&page=foo"))
      )

      status(result)                                                   shouldBe Status.BAD_REQUEST
      contentType(result)                                              shouldBe Some("text/html")
      charset(result)                                                  shouldBe Some("utf-8")
      bodyOf(result)                                                   should include("search-heading")
      asDocument(bodyOf(result)).getElementById("search-heading").text shouldBe messageApi("search.heading")

      verifyZeroInteractions(rulingService)
    }

    "return 303 when disallowed" in {
      val result = await(
        controller(allowlist = AllowListEnabled()).get(query = None, imagesOnly = false, page = 1)(getRequestWithCSRF())
      )

      status(result) shouldBe Status.SEE_OTHER
    }

    "return 429 when too many requests are made" in {
      given(appConfig.rateLimiterEnabled) willReturn true
      given(appConfig.rateLimitBucketSize) willReturn 5
      given(appConfig.rateLimitRatePerSecond) willReturn 2
      val results = for (_ <- 0 until 100) yield controller().get(Some("foo"), false, 1)(getRequestWithCSRF())
      val statuses = await(Future.sequence(results)).map(status)
      atLeast(1, statuses) shouldBe Status.TOO_MANY_REQUESTS
    }

    "return 200 when rate limiting is disabled" in {
      given(appConfig.rateLimiterEnabled) willReturn false
      val results = for (_ <- 0 until 100) yield controller().get(Some("foo"), false, 1)(getRequestWithCSRF())
      val statuses = await(Future.sequence(results)).map(status)
      all(statuses) shouldBe Status.OK
    }
  }

}
