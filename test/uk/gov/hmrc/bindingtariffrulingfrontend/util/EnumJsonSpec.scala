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

package uk.gov.hmrc.bindingtariffrulingfrontend.util

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

import play.api.libs.json._
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.ApplicationType
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.ApplicationType.BTI

class EnumJsonSpec extends UnitSpec {

  "EnumJson" when {

    ".enumReads" should {

      "return JsSuccess" when {
        "a JSON with a valid enum name is read" in {
          Json.fromJson[ApplicationType.Value](JsString("BTI")) shouldBe JsSuccess(BTI)
        }
      }

      "return JsError" when {
        "a JSON with an invalid enum name is read" in {
          Json.fromJson[ApplicationType.Value](JsString("enum")) shouldBe JsError(
            "Expected an enumeration of type: 'ApplicationType$', but it does not contain the name: 'enum'"
          )
        }

        "a JSON with a number value is read" in {
          Json.fromJson[ApplicationType.Value](JsNumber(1)) shouldBe JsError("String value is expected")
        }
      }
    }

    ".enumWrites" should {
      "return the expected JSON string" in {
        Json.toJson[ApplicationType.Value](BTI) shouldBe JsString("BTI")
      }
    }
  }
}
