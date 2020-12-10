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

import java.time.Instant

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action._
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.bindingtariffrulingfrontend.service.{FileStoreService, RulingService}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata

class RulingControllerSpec extends ControllerSpec {

  private val rulingService    = mock[RulingService]
  private val fileStoreService = mock[FileStoreService]

  private def controller(
    allowlist: AllowedAction  = AllowListDisabled(),
    auth: AuthenticatedAction = SuccessfulAuth(),
    admin: AdminAction        = AdminEnabled()
  ) =
    new RulingController(rulingService, fileStoreService, allowlist, auth, admin, mcc, realConfig)

  "GET /" should {
    "return 200" in {
      given(rulingService.get("id")) willReturn Future.successful(
        Some(Ruling("ref", "code", Instant.now, Instant.now, "justification", "goods description"))
      )
      given(fileStoreService.get(any[Ruling])(any[HeaderCarrier])).willReturn(Future.successful(Map.empty[String, FileMetadata]))

      val result = await(controller().get("id")(getRequestWithCSRF()))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)      should include("ruling-heading")
    }

    "return 200 - when not found" in {
      given(rulingService.get("id")) willReturn Future.successful(None)

      val result = await(controller().get("id")(getRequestWithCSRF()))
      status(result)      shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result)     shouldBe Some("utf-8")
      bodyOf(result)      should include("ruling_not_found-heading")
    }

    "return 403 when not allowed" in {
      val result = await(controller(allowlist = AllowListEnabled()).get("id")(getRequestWithCSRF()))
      status(result) shouldBe Status.FORBIDDEN
    }

  }

  "POST /" should {
    "return 202 when authenticated" in {
      given(rulingService.refresh(refEq("id"))(any[HeaderCarrier])) willReturn Future.successful(())
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
      given(rulingService.deleteAll()) willReturn Future.successful(())
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
      given(rulingService.delete(refEq("ref"))) willReturn Future.successful(())
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
