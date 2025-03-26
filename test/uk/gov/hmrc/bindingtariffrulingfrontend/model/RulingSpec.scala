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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

import java.time.Instant
import play.api.libs.json.{JsArray, JsError, JsNull, JsString, JsSuccess, Json}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class RulingSpec extends BaseSpec {

  val ruling: Ruling = Ruling(
    reference = "reference",
    bindingCommodityCode = "0011223344",
    effectiveStartDate = Instant.EPOCH,
    effectiveEndDate = Instant.EPOCH.plusSeconds(1),
    justification = "justification",
    goodsDescription = "goods-description",
    keywords = Set("keyword"),
    attachments = Seq("attachment"),
    images = Seq("image")
  )

  val rulingDefault = ruling.copy(keywords = Set.empty, attachments = Seq.empty, images = Seq.empty)

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

    "Convert from REST Json" in {
      val json = Json.obj(
        "reference"            -> "reference",
        "bindingCommodityCode" -> "0011223344",
        "effectiveStartDate"   -> "1970-01-01T00:00:00Z",
        "effectiveEndDate"     -> "1970-01-01T00:00:01Z",
        "justification"        -> "justification",
        "goodsDescription"     -> "goods-description",
        "keywords"             -> Seq("keyword"),
        "attachments"          -> Seq("attachment"),
        "images"               -> Seq("image")
      )

      val result = json.as[Ruling](Ruling.REST.format)
      result shouldBe ruling
    }

    "Convert from REST Json with default values" in {
      val json = Json.obj(
        "reference"            -> "reference",
        "bindingCommodityCode" -> "0011223344",
        "effectiveStartDate"   -> "1970-01-01T00:00:00Z",
        "effectiveEndDate"     -> "1970-01-01T00:00:01Z",
        "justification"        -> "justification",
        "goodsDescription"     -> "goods-description"
      )

      val result = json.as[Ruling](Ruling.REST.format)
      result shouldBe rulingDefault
    }

    "exercise Mongo formatter with default values" in {
      val ruling = Ruling(
        reference = "test-ref",
        bindingCommodityCode = "code123",
        effectiveStartDate = Instant.EPOCH,
        effectiveEndDate = Instant.EPOCH.plusSeconds(1000),
        justification = "test justification",
        goodsDescription = "test description",
        keywords = Set("keyword1", "keyword2"),
        attachments = Seq("attachment1"),
        images = Seq("image1")
      )

      val mongoReads  = Ruling.Mongo.rulingReads
      val mongoWrites = Ruling.Mongo.rulingWrites

      val json = mongoWrites.writes(ruling)

      val rulingFromJson = mongoReads.reads(json).get
      rulingFromJson shouldBe ruling

      val jsonWithMissingFields = Json.obj(
        "reference"            -> "test-ref",
        "bindingCommodityCode" -> "code123",
        "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
        "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
        "justification"        -> "test justification",
        "goodsDescription"     -> "test description"
      )

      val rulingWithDefaults = mongoReads.reads(jsonWithMissingFields).get
      rulingWithDefaults.reference   shouldBe "test-ref"
      rulingWithDefaults.keywords    shouldBe Set.empty[String] // Default value
      rulingWithDefaults.attachments shouldBe Seq.empty[String] // Default value
      rulingWithDefaults.images      shouldBe Seq.empty[String] // Default value
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
        "effectiveStartDate" -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
        "effectiveEndDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
        "justification"      -> JsString("justification"),
        "goodsDescription"   -> JsString("goods-description"),
        "keywords"           -> JsArray(Seq(JsString("keyword"))),
        "attachments"        -> JsArray(Seq(JsString("attachment"))),
        "images"             -> JsArray(Seq(JsString("image")))
      )
    }

    "Ruling REST.format" should {
      "error when JSON is invalid" in {
        Json.arr().validate[Ruling](Ruling.REST.format) shouldBe a[JsError]
      }

      "error with an empty JSON object" in {
        val json = Json.obj()
        json.validate[Ruling](Ruling.REST.format) shouldBe a[JsError]
      }

      "error when required fields are missing" in {
        val incompleteJson = Json.obj(
          "reference" -> "reference"
        )
        incompleteJson.validate[Ruling](Ruling.REST.format) shouldBe a[JsError]
      }
    }

    "Mongo.rulingReads" should {
      "handle apply operation" in {
        val formatter = Ruling.Mongo.rulingReads

        val json = Json.obj(
          "reference"            -> "test-ref",
          "bindingCommodityCode" -> "1234",
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test desc"
        )

        val result = formatter.reads(json)
        result.isSuccess shouldBe true
      }

      "handle writes operation" in {
        val fullFormat = Ruling.Mongo.format

        val testRuling = ruling.copy(
          reference = "test-writes",
          keywords = Set("key1", "key2")
        )

        val json = fullFormat.writes(testRuling)

        (json \ "reference").as[String]            shouldBe "test-writes"
        (json \ "bindingCommodityCode").as[String] shouldBe "0011223344"
      }

      "handle refI operation" in {
        val formatter  = Ruling.Mongo.rulingReads
        val fullFormat = Ruling.Mongo.format

        val testRuling = ruling.copy(reference = "test-refI")
        val json       = fullFormat.writes(testRuling)

        val result = formatter.reads(json)
        result.isSuccess     shouldBe true
        result.get.reference shouldBe "test-refI"
      }

      "handle arrow operation" in {
        val fullFormat = Ruling.Mongo.format

        val testRuling = ruling.copy(reference = "test-arrow")
        val json       = fullFormat.writes(testRuling)

        (json \ "reference").as[String]            shouldBe "test-arrow"
        (json \ "bindingCommodityCode").as[String] shouldBe "0011223344"
      }

      "handle newBuilder operation" in {
        val formatter = Ruling.Mongo.rulingReads

        val builderJson = Json.obj(
          "reference"            -> "builder-test",
          "bindingCommodityCode" -> "5678",
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test description"
        )

        val result = formatter.reads(builderJson)
        result.isSuccess     shouldBe true
        result.get.reference shouldBe "builder-test"
      }

      "use default values for missing fields" in {
        val formatter = Ruling.Mongo.rulingReads

        val minimalJson = Json.obj(
          "reference"            -> "minimal",
          "bindingCommodityCode" -> "minimal",
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "minimal",
          "goodsDescription"     -> "minimal"
        )

        val result = formatter.reads(minimalJson)
        result.isSuccess       shouldBe true
        result.get.keywords    shouldBe Set.empty
        result.get.attachments shouldBe Seq.empty
        result.get.images      shouldBe Seq.empty
      }
    }

    "Ruling.Mongo.rulingReads" should {
      val formatter = Ruling.Mongo.rulingReads

      "read JSON with default values" in {
        val minimalJson = Json.obj(
          "reference"            -> "default-test",
          "bindingCommodityCode" -> "abc123",
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test description"
        )

        val result = formatter.reads(minimalJson)

        result.isSuccess shouldBe true
        val ruling = result.get
        ruling.keywords    shouldBe Set.empty
        ruling.attachments shouldBe Seq.empty
        ruling.images      shouldBe Seq.empty
      }

      "handle invalid JSON values" in {
        val invalidDateJson = Json.obj(
          "reference"            -> "invalid-date",
          "bindingCommodityCode" -> "abc123",
          "effectiveStartDate"   -> Json.obj("invalid" -> "date"),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test description"
        )

        val dateResult = formatter.reads(invalidDateJson)
        dateResult.isError shouldBe true

        val missingFieldJson = Json.obj(
          "reference"          -> "missing-field",
          "effectiveStartDate" -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"      -> "test",
          "goodsDescription"   -> "test description"
        )

        val missingResult = formatter.reads(missingFieldJson)
        missingResult.isError shouldBe true

        val arrayJson   = Json.arr("not", "an", "object")
        val arrayResult = formatter.reads(arrayJson)
        arrayResult.isError shouldBe true
      }

      "handle JSON with wrong field types" in {
        val wrongTypesJson = Json.obj(
          "reference"            -> 12345, // Should be string
          "bindingCommodityCode" -> "abc123",
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test description"
        )

        val typeResult = formatter.reads(wrongTypesJson)
        typeResult.isError shouldBe true

        val nullValuesJson = Json.obj(
          "reference"            -> "null-test",
          "bindingCommodityCode" -> JsNull, // Null value
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test description"
        )

        val nullResult = formatter.reads(nullValuesJson)
        nullResult.isError shouldBe true
      }

      "handle JSON with extra fields" in {
        val extraFieldsJson = Json.obj(
          "reference"            -> "extra-fields",
          "bindingCommodityCode" -> "abc123",
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test description",
          "extraField1"          -> "should be ignored",
          "extraField2"          -> 12345,
          "extraField3"          -> Json.obj("nested" -> "value")
        )

        val extraResult = formatter.reads(extraFieldsJson)
        extraResult.isSuccess     shouldBe true
        extraResult.get.reference shouldBe "extra-fields"
      }

      "handle empty arrays and collections" in {
        // JSON with empty arrays
        val emptyCollectionsJson = Json.obj(
          "reference"            -> "empty-collections",
          "bindingCommodityCode" -> "abc123",
          "effectiveStartDate"   -> Json.obj("$date" -> Json.obj("$numberLong" -> "0")),
          "effectiveEndDate"     -> Json.obj("$date" -> Json.obj("$numberLong" -> "1000")),
          "justification"        -> "test",
          "goodsDescription"     -> "test description",
          "keywords"             -> Json.arr(),
          "attachments"          -> Json.arr(),
          "images"               -> Json.arr()
        )

        val emptyResult = formatter.reads(emptyCollectionsJson)
        emptyResult.isSuccess shouldBe true
        val ruling = emptyResult.get
        ruling.keywords    shouldBe Set.empty
        ruling.attachments shouldBe Seq.empty
        ruling.images      shouldBe Seq.empty
      }
    }

    "error when required fields are missing" in {
      val incompleteJson = Json.obj(
        "reference" -> "reference"
      )
      incompleteJson.validate[Ruling](Ruling.Mongo.rulingReads) shouldBe a[JsError]
    }

  }

}
