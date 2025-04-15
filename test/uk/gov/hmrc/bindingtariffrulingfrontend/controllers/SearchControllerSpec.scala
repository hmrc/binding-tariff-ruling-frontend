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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{mock, reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers.*
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.filters.RateLimitFilter
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.{FileStoreService, RulingService}
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SearchControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val appConfig        = mock(classOf[AppConfig])
  private val rulingService    = mock(classOf[RulingService])
  private val fileStoreService = mock(classOf[FileStoreService])
  private val rateLimit        = new RateLimitFilter(appConfig)
  private val searchView       = app.injector.instanceOf[views.html.search]

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(rulingService)
    reset(fileStoreService)
  }

  private def controller(): SearchController =
    new SearchController(rulingService, fileStoreService, rateLimit, mcc, searchView, realConfig)

  "GET /" should {
    "return 200 with a valid query" in {
      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(
        controller()
          .get(query = Some("query"), images = false, page = 1)(
            getRequestWithCSRF("/?query=query&page=1").withFormUrlEncodedBody(
              "query" -> "query"
            )
          )
      )

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messageApi("search.heading"))

      verify(rulingService).get(SimpleSearch(Some("query"), imagesOnly = false, 1))
      verify(fileStoreService).get(refEq(Paged.empty[Ruling]))(any[HeaderCarrier])
    }

    "return 200 with no search query" in {

      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(controller().get(query = None, images = false, page = 1)(getRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messageApi("search.heading"))

      verify(rulingService).get(SimpleSearch(None, imagesOnly = false, 1))
      verify(fileStoreService).get(refEq(Paged.empty[Ruling]))(any[HeaderCarrier])
    }

    "return 200 with an empty search query" in {
      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(controller().get(query = Some(""), images = false, page = 1)(getRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messageApi("search.heading"))

      verify(rulingService).get(SimpleSearch(None, imagesOnly = false, 1))
      verify(fileStoreService).get(refEq(Paged.empty[Ruling]))(any[HeaderCarrier])
    }

    "return 429 when too many requests are made" in {
      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      when(appConfig.rateLimiterEnabled).thenReturn(true)
      when(appConfig.rateLimitBucketSize).thenReturn(5)
      when(appConfig.rateLimitRatePerSecond).thenReturn(2)
      val results  = for (_ <- 0 until 100) yield controller().get(Some("foo"), images = false, 1)(getRequestWithCSRF())
      val statuses = await(Future.sequence(results)).map(status)
      atLeast(1, statuses) shouldBe Status.TOO_MANY_REQUESTS
    }

    "return 200 when rate limiting is disabled" in {
      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      when(appConfig.rateLimiterEnabled).thenReturn(false)
      val results  = for (_ <- 0 until 100) yield controller().get(Some("foo"), images = false, 1)(getRequestWithCSRF())
      val statuses = await(Future.sequence(results)).map(status)
      all(statuses) shouldBe Status.OK
    }
  }

  "searchRuling" should {
    "return 200 with a valid query" in {
      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(
        controller()
          .get(query = Some("query"), images = false, page = 1)(
            getRequestWithCSRF().withFormUrlEncodedBody(
              "query" -> "query"
            )
          )
      )

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messageApi("search.heading"))

      verify(rulingService).get(SimpleSearch(Some("query"), imagesOnly = false, 1))
      verify(fileStoreService).get(refEq(Paged.empty[Ruling]))(any[HeaderCarrier])
    }

    "return 200 with no search query" in {

      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(controller().get(query = None, images = false, page = 1)(getRequestWithCSRF()))

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messageApi("search.heading"))

      verify(rulingService).get(SimpleSearch(None, imagesOnly = false, 1))
      verify(fileStoreService).get(refEq(Paged.empty[Ruling]))(any[HeaderCarrier])
    }

    "return a form with error when a blank search query is passed" in {

      when(rulingService.get(any[SimpleSearch])).thenReturn(Future.successful(Paged.empty[Ruling]))
      when(fileStoreService.get(any[Paged[Ruling]])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(
        controller()
          .get(query = Some(""), images = false, page = 1)(
            getRequestWithCSRF().withFormUrlEncodedBody(
              "query" -> ""
            )
          )
      )

      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messageApi("search.heading"))
      bodyOf(result)        should include(messageApi("Enter a search term"))

      verify(rulingService).get(SimpleSearch(None, imagesOnly = false, 1))
      verify(fileStoreService).get(refEq(Paged.empty[Ruling]))(any[HeaderCarrier])
    }
  }
}
