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

import java.time.Instant

import akka.stream.Materializer
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import play.api.http.Status
import play.api.i18n.{DefaultLangs, DefaultMessagesApi}
import play.api.test.Helpers._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action._
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future


class RulingControllerSpec extends ControllerSpec {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val env = Environment.simple()
  private val configuration = Configuration.load(env)

  private val messageApi = new DefaultMessagesApi(env, configuration, new DefaultLangs(configuration))
  private val appConfig = new AppConfig(configuration, env)
  private implicit val mat: Materializer = fakeApplication.materializer
  private val rulingService = mock[RulingService]

  private def controller(whitelist: WhitelistedAction = WhitelistDisabled(),
                         auth: AuthenticatedAction = SuccessfulAuth(),
                         admin: AdminAction = AdminEnabled()
                        ) = new RulingController(rulingService, whitelist, auth, admin, messageApi, appConfig)

  "GET /" should {
    "return 200" in {
      given(rulingService.get("id")) willReturn Future.successful(Some(Ruling("ref", "code", Instant.now, Instant.now, "justification", "goods description")))

      val result = await(controller().get("id")(getRequestWithCSRF()))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("ruling-heading")
    }

    "return 200 - when not found" in {
      given(rulingService.get("id")) willReturn Future.successful(None)

      val result = await(controller().get("id")(getRequestWithCSRF()))
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
      bodyOf(result) should include("ruling_not_found-heading")
    }

    "return 403 when not whitelisted" in {
      val result = await(controller(whitelist = WhitelistEnabled()).get("id")(getRequestWithCSRF()))
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
      given(rulingService.delete()) willReturn Future.successful(())
      val result = await(controller(auth = SuccessfulAuth(), admin = AdminEnabled()).delete()(postRequestWithCSRF))
      status(result) shouldBe Status.NO_CONTENT
    }

    "return 403 when unauthenticated" in {
      val result = await(controller(auth = FailedAuth(), admin = AdminEnabled()).delete()(postRequestWithCSRF))
      status(result) shouldBe Status.FORBIDDEN
    }

    "return 403 when admin disabled" in {
      val result = await(controller(auth = SuccessfulAuth(), admin = AdminDisabled()).delete()(postRequestWithCSRF))
      status(result) shouldBe Status.FORBIDDEN
    }

  }
}