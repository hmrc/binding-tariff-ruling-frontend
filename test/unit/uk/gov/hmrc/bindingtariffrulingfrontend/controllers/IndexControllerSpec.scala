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

import play.api.http.Status
import play.api.test.Helpers._
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.{WhitelistDisabled, WhitelistEnabled, WhitelistedAction}

class IndexControllerSpec extends ControllerSpec {

  private def controller(whitelist: WhitelistedAction = WhitelistDisabled()) = new IndexController(whitelist, mcc, realConfig)

  "GET /" should {
    "return 200" in {
      val result = controller().get(getRequestWithCSRF())
      status(result) shouldBe Status.OK
      contentType(result) shouldBe Some("text/html")
      charset(result) shouldBe Some("utf-8")
    }

    "return 403 when whitelisted" in {
      val result = controller(WhitelistEnabled()).get(getRequestWithCSRF())
      status(result) shouldBe Status.FORBIDDEN
    }

  }
}