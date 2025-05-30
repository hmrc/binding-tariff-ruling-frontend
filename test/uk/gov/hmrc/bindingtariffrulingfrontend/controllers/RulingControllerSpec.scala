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
import org.mockito.Mockito.{mock, when}
import play.api.http.Status
import play.api.test.Helpers.*
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.*
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.bindingtariffrulingfrontend.service.{FileStoreService, RulingService}
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RulingControllerSpec extends ControllerSpec {

  private val rulingService    = mock(classOf[RulingService])
  private val fileStoreService = mock(classOf[FileStoreService])
  private val rulingView       = app.injector.instanceOf[views.html.ruling]
  private val notFoundView     = app.injector.instanceOf[views.html.not_found]

  private def controller(
    auth: AuthenticatedAction = SuccessfulAuth(),
    admin: AdminAction = AdminEnabled()
  ) =
    new RulingController(
      rulingService,
      fileStoreService,
      auth,
      admin,
      rulingView,
      notFoundView,
      mcc,
      realConfig
    )

  "GET /" should {
    "return 200" in {
      when(rulingService.get("id")).thenReturn(
        Future.successful(
          Some(Ruling("ref", "code", Instant.now, Instant.now, "justification", "goods description"))
        )
      )
      when(fileStoreService.get(any[Ruling])(any[HeaderCarrier]))
        .thenReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(controller().get("id")(getRequestWithCSRF()))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include(messageApi("ruling.heading", "ref"))
    }

    "return 404 - when not found" in {
      when(rulingService.get("id")).thenReturn(Future.successful(None))

      val result = await(controller().get("id")(getRequestWithCSRF()))
      status(result)      shouldBe Status.NOT_FOUND
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)        should include("not_found-heading")
    }
  }

  "POST /" should {
    "return 202 when authenticated" in {
      when(rulingService.refresh(refEq("id"))(any[HeaderCarrier])).thenReturn(Future.successful(()))
      val result = await(controller(auth = SuccessfulAuth()).post("id")(postRequestWithCSRF))
      status(result) shouldBe Status.ACCEPTED
    }

    "return 403 when unauthenticated" in {
      val result = await(controller(auth = FailedAuth()).post("id")(postRequestWithCSRF))
      status(result) shouldBe Status.FORBIDDEN
    }

  }

  "DELETE /" should {
    "return 204 when authenticated" in {
      when(rulingService.deleteAll()).thenReturn(Future.successful(()))
      val result = await(controller(auth = SuccessfulAuth(), admin = AdminEnabled()).deleteAll()(postRequestWithCSRF))
      status(result) shouldBe Status.NO_CONTENT
    }

    "return 403 when unauthenticated" in {
      val result = await(controller(auth = FailedAuth(), admin = AdminEnabled()).deleteAll()(postRequestWithCSRF))
      status(result) shouldBe Status.FORBIDDEN
    }

    "return 403 when admin disabled" in {
      val result = await(controller(auth = SuccessfulAuth(), admin = AdminDisabled()).deleteAll()(postRequestWithCSRF))
      status(result) shouldBe Status.FORBIDDEN
    }

  }

  "DELETE / $id" should {
    "return 204 when authenticated" in {
      when(rulingService.delete(refEq("ref"))).thenReturn(Future.successful(()))
      val result = await(controller(auth = SuccessfulAuth(), admin = AdminEnabled()).delete("ref")(postRequestWithCSRF))
      status(result) shouldBe Status.NO_CONTENT
    }

    "return 403 when unauthenticated" in {
      val result = await(controller(auth = FailedAuth(), admin = AdminEnabled()).delete("ref")(postRequestWithCSRF))
      status(result) shouldBe Status.FORBIDDEN
    }

    "return 403 when admin disabled" in {
      val result =
        await(controller(auth = SuccessfulAuth(), admin = AdminDisabled()).delete("ref")(postRequestWithCSRF))
      status(result) shouldBe Status.FORBIDDEN
    }

  }
}
