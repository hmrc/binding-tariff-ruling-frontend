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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector.model

import org.scalatest.matchers.must.Matchers.{must, mustBe}
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class ApplicationSpec extends BaseSpec {

  "Application case class" should {

    "correctly serialize to JSON" in {
      val appInstance = Application(`type` = ApplicationType.BTI)

      val expectedJson = Json.obj("type" -> "BTI")

      val jsonResult = Json.toJson(appInstance)

      jsonResult mustBe expectedJson
    }

    "correctly deserialize from JSON" in {
      val json = Json.obj("type" -> "BTI")

      val expectedInstance = Application(`type` = ApplicationType.BTI)

      val result = json.as[Application]

      result mustBe expectedInstance
    }

    "ensure equality of identical instances" in {
      val app1 = Application(`type` = ApplicationType.BTI)
      val app2 = Application(`type` = ApplicationType.BTI)

      app1 mustBe app2
    }

    "ensure inequality for different instances" in {
      val app1 = Application(`type` = ApplicationType.BTI)
      val app2 = Application(`type` = ApplicationType.LIABILITY_ORDER) // Different type

      app1 must not be app2
    }

    "serialize Application to JSON correctly using outboundFormat" in {
      val appInstance = Application(`type` = ApplicationType.BTI)

      val expectedJson = Json.obj("type" -> "BTI")

      val jsonResult = Json.toJson(appInstance)(Application.outboundFormat)

      jsonResult mustBe expectedJson
    }

    "deserialize JSON to Application correctly using outboundFormat" in {
      val json = Json.obj("type" -> "BTI")

      val expectedInstance = Application(`type` = ApplicationType.BTI)

      val result = json.as[Application](Application.outboundFormat)

      result mustBe expectedInstance
    }

    "fail when deserializing an invalid ApplicationType using outboundFormat" in {
      val invalidJson = Json.obj("type" -> "INVALID_TYPE")

      intercept[JsResultException] {
        invalidJson.as[Application](Application.outboundFormat)
      }
    }

    "support round-trip serialization/deserialization using outboundFormat" in {
      val appInstance = Application(`type` = ApplicationType.BTI)

      val json   = Json.toJson(appInstance)(Application.outboundFormat)
      val result = json.as[Application](Application.outboundFormat)

      result mustBe appInstance
    }

    "serialize and deserialize Application with an unknown type" in {
      val unknownJson = Json.obj("type" -> "UNKNOWN")

      val jsonValidation = Json.fromJson[Application](unknownJson)(Application.outboundFormat)

      jsonValidation.isError mustBe true
    }

    "handle case where JSON is missing the type field" in {
      val missingFieldJson = Json.obj()

      val jsonValidation = Json.fromJson[Application](missingFieldJson)(Application.outboundFormat)

      jsonValidation.isError mustBe true
    }

  }
}
