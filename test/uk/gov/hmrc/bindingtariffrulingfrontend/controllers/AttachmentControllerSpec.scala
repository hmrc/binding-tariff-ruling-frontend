/*
 * Copyright 2024 HM Revenue & Customs
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

import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.mockito.Mockito
import org.mockito.Mockito.{mock, reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.service.FileStoreService
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.http.HeaderCarrier

import java.nio.charset.StandardCharsets
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AttachmentControllerSpec extends ControllerSpec with BeforeAndAfterEach {

  private val fileStoreService            = mock(classOf[FileStoreService])
  override lazy val realConfig: AppConfig = mock(classOf[AppConfig])
  private val notFoundView                = app.injector.instanceOf[views.html.not_found]

  private def controller(): AttachmentController =
    new AttachmentController(fileStoreService, mcc, notFoundView, realConfig)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(fileStoreService)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset(realConfig)
    given(realConfig.displayImages).willReturn(true)
  }

  "GET /" should {
    val rulingReference = "600000001"
    val fileId          = "d4897c0a-b92d-4cf7-8990-f40fe158be68"

    val metadata = FileMetadata(
      id = fileId,
      fileName = Some("some.png"),
      mimeType = Some("image/png"),
      url = Some("http://localhost:4572/digital-tariffs-local/d4897c0a-b92d-4cf7-8990-f40fe158be68"),
      published = true
    )

    "return 303 when given a valid attachment id (images toggled off)" in {
      given(realConfig.displayImages).willReturn(false)

      val url     = metadata.url.get
      val pngData = "png data".getBytes(StandardCharsets.UTF_8)
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(Some(metadata))
      given(fileStoreService.downloadFile(refEq(url))(any[HeaderCarrier])) willReturn Future.successful(
        Some(Source.single(ByteString(pngData)))
      )
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result) shouldBe Status.SEE_OTHER
    }

    "return 200 when given a valid attachment id" in {
      val url     = metadata.url.get
      val pngData = "png data".getBytes(StandardCharsets.UTF_8)
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(Some(metadata))
      given(fileStoreService.downloadFile(refEq(url))(any[HeaderCarrier])) willReturn Future.successful(
        Some(Source.single(ByteString(pngData)))
      )
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result)         shouldBe Status.OK
      contentType(result)    shouldBe Some("image/png")
      contentAsBytes(result) shouldBe pngData

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService).downloadFile(refEq(url))(any[HeaderCarrier])
    }

    "return 404 when there is no file metadata" in {
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(None)
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("not_found-heading")

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService, times(0)).downloadFile(any[String])(any[HeaderCarrier])
    }

    "return 404 when the filestore service does not respond normally to get" in {
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.failed(new Exception)
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result) shouldBe Status.BAD_GATEWAY

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService, times(0)).downloadFile(any[String])(any[HeaderCarrier])
    }

    "return 404 when the filestore service does not respond normally to downloadFile" in {
      val url = metadata.url.get
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(Some(metadata))
      given(fileStoreService.downloadFile(refEq(url))(any[HeaderCarrier])) willReturn Future.failed(new Exception)
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result) shouldBe Status.BAD_GATEWAY

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService).downloadFile(refEq(url))(any[HeaderCarrier])
    }

    "return 404 when the file metadata contains no url" in {
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(
        Some(metadata.copy(url = None))
      )
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("not_found-heading")

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService, times(0)).downloadFile(any[String])(any[HeaderCarrier])
    }

    "return 404 when the file metadata contains no filename" in {
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(
        Some(metadata.copy(fileName = None))
      )
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("not_found-heading")

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService, times(0)).downloadFile(any[String])(any[HeaderCarrier])
    }

    "return 404 when the file metadata contains no mime type" in {
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(
        Some(metadata.copy(mimeType = None))
      )
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("not_found-heading")

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService, times(0)).downloadFile(any[String])(any[HeaderCarrier])
    }

    "return 404 when the url in file metadata does not return any data" in {
      val url = metadata.url.get
      given(fileStoreService.get(any[String])(any[HeaderCarrier])) willReturn Future.successful(Some(metadata))
      given(fileStoreService.downloadFile(refEq(url))(any[HeaderCarrier])) willReturn Future.successful(None)
      val result = await(controller().get(rulingReference, fileId)(getRequestWithCSRF()))

      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("not_found-heading")

      verify(fileStoreService).get(refEq(fileId))(any[HeaderCarrier])
      verify(fileStoreService).downloadFile(refEq(url))(any[HeaderCarrier])
    }

  }
}
