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

import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class AttachmentSpec extends BaseSpec {

  "Attachment case class" should {

    "construct an instance using apply" in {
      val attachment = Attachment(id = "12345", public = true, shouldPublishToRulings = true)

      attachment.id mustBe "12345"
      attachment.public mustBe true
      attachment.shouldPublishToRulings mustBe true
    }

  }

  "Attachment JSON format" should {

    "serialize Attachment to JSON" in {
      val attachment = Attachment(id = "12345", public = true, shouldPublishToRulings = false)
      val expectedJson = Json.obj(
        "id"                     -> "12345",
        "public"                 -> true,
        "shouldPublishToRulings" -> false
      )

      val jsonResult = Json.toJson(attachment)(Attachment.outboundFormat)

      jsonResult mustBe expectedJson
    }

    "deserialize JSON to Attachment" in {
      val json = Json.obj(
        "id"                     -> "12345",
        "public"                 -> false,
        "shouldPublishToRulings" -> true
      )
      val expectedInstance = Attachment(id = "12345", public = false, shouldPublishToRulings = true)

      val result = json.as[Attachment](Attachment.outboundFormat)

      result mustBe expectedInstance
    }

    "support default values when fields are missing" in {
      val json = Json.obj("id" -> "12345")

      val result = json.as[Attachment](Attachment.outboundFormat)

      result mustBe Attachment(id = "12345", public = false, shouldPublishToRulings = false)
    }

    "fail when deserializing with missing id" in {
      val invalidJson = Json.obj(
        "public"                 -> true,
        "shouldPublishToRulings" -> true
      )

      intercept[JsResultException] {
        invalidJson.as[Attachment](Attachment.outboundFormat)
      }
    }

    "support round-trip serialization/deserialization" in {
      val attachment = Attachment(id = "A123", public = true, shouldPublishToRulings = false)

      val json   = Json.toJson(attachment)(Attachment.outboundFormat)
      val result = json.as[Attachment](Attachment.outboundFormat)

      result mustBe attachment
    }
  }
}
