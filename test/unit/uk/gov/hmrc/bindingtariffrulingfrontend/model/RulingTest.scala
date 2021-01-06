/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

import java.time.Instant

import play.api.libs.json.{JsArray, JsString, Json}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class RulingTest extends BaseSpec {

  val ruling: Ruling = Ruling(
    reference            = "reference",
    bindingCommodityCode = "0011223344",
    effectiveStartDate   = Instant.EPOCH,
    effectiveEndDate     = Instant.EPOCH.plusSeconds(1),
    justification        = "justification",
    goodsDescription     = "goods-description",
    keywords             = Set("keyword"),
    attachments          = Seq("attachment"),
    images               = Seq("image")
  )

  "Ruling" should {
    "Convert to REST Json" in {
      Json.toJson(ruling)(Ruling.REST.format) shouldBe Json.obj(
        "reference"            -> JsString("reference"),
        "bindingCommodityCode" -> JsString("0011223344"),
        "effectiveStartDate"   -> JsString("1970-01-01T00:00:00Z"),
        "effectiveEndDate"     -> JsString("1970-01-01T00:00:01Z"),
        "justification"        -> JsString("justification"),
        "goodsDescription"     -> JsString("goods-description"),
        "keywords"             -> JsArray(Seq(JsString("keyword"))),
        "attachments"          -> JsArray(Seq(JsString("attachment"))),
        "images"               -> JsArray(Seq(JsString("image")))
      )
    }

    "Convert to Mongo Json" in {
      Json.toJson(ruling)(Ruling.Mongo.format) shouldBe Json.obj(
        "reference"            -> JsString("reference"),
        "bindingCommodityCode" -> JsString("0011223344"),
        "bindingCommodityCodeNGrams" -> Json.arr(
          "0",
          "00",
          "001",
          "0011",
          "00112",
          "001122",
          "0011223",
          "00112233",
          "001122334",
          "0011223344"
        ),
        "effectiveStartDate" -> Json.obj("$date" -> Json.obj("$numberLong" -> 0)),
        "effectiveEndDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> 1000)),
        "justification"      -> JsString("justification"),
        "goodsDescription"   -> JsString("goods-description"),
        "keywords"           -> JsArray(Seq(JsString("keyword"))),
        "attachments"        -> JsArray(Seq(JsString("attachment"))),
        "images"             -> JsArray(Seq(JsString("image")))
      )
    }
  }

}
