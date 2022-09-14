/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Paged.{reads, writes}

class PagedTest extends BaseSpec {

  "Paged" should {
    "map" in {
      Paged(Seq("hello")).map(_.toUpperCase) shouldBe Paged(Seq("HELLO"))
    }

    "calculate size" in {
      Paged.empty.size                     shouldBe 0
      Paged.empty(SimplePagination()).size shouldBe 0
      Paged(Seq("")).size                  shouldBe 1
    }

    "calculate isEmpty" in {
      Paged.empty.isEmpty                     shouldBe true
      Paged.empty(SimplePagination()).isEmpty shouldBe true
      Paged(Seq("")).isEmpty                  shouldBe false
    }

    // scalastyle:off magic.number
    "calculate pageCount" in {
      Paged.empty.pageCount shouldBe 0
      Paged(results = Seq(), pageIndex   = 1, pageSize = 1, resultCount = 1).pageCount shouldBe 1
      Paged(results = Seq(), pageIndex   = 1, pageSize = 1, resultCount = 2).pageCount shouldBe 2
      Paged(results = Seq(), pageIndex   = 1, pageSize = 2, resultCount = 3).pageCount shouldBe 2
      Paged(results = Seq(), pageIndex   = 1, pageSize = 10, resultCount = 100).pageCount shouldBe 10
      Paged(results = Seq(), pageIndex   = 1, pageSize = 1, resultCount = 100).pageCount shouldBe 100
      Paged(results = Seq(), resultCount = 1).pageCount shouldBe 1
      Paged(results = Seq(), resultCount = 100).pageCount shouldBe 1
      Paged(results = Seq(), resultCount = 1000).pageCount shouldBe 10
    }
    // scalastyle:on magic.number

    "calculate nonEmpty" in {
      Paged.empty.nonEmpty                     shouldBe false
      Paged.empty(SimplePagination()).nonEmpty shouldBe false
      Paged(Seq("")).nonEmpty                  shouldBe true
    }

    "valid results when results are provided" in {
      val validPaged = Json
        .obj(
          "results"     -> Seq(""),
          "pageIndex"   -> 1,
          "pageSize"    -> 1,
          "resultCount" -> 1
        )
        .as[Paged[String]]

      validPaged shouldBe Paged(Seq(""), 1, 1, 1)
    }

    "invalid results when no results provided" in {
      val exception = intercept[JsResultException] {
        Json
          .obj(
            "pageIndex"   -> 1,
            "pageSize"    -> 1,
            "resultCount" -> 1
          )
          .as[Paged[String]]
      }

      exception.errors.size                              shouldBe 1
      exception.errors.flatMap(_._2.flatMap(_.messages)) shouldBe List("invalid results")
    }

    "invalid json when no pageIndex provided" in {
      val exception = intercept[JsResultException] {
        Json
          .obj(
            "results"     -> Seq(""),
            "pageSize"    -> 1,
            "resultCount" -> 1
          )
          .as[Paged[String]]
      }

      exception.errors.size                              shouldBe 1
      exception.errors.flatMap(_._2.flatMap(_.messages)) shouldBe List("invalid pageIndex")
    }

    "invalid json when no pageSize provided" in {
      val exception = intercept[JsResultException] {
        Json
          .obj(
            "results"     -> Seq(""),
            "pageIndex"   -> 1,
            "resultCount" -> 1
          )
          .as[Paged[String]]
      }

      exception.errors.size                              shouldBe 1
      exception.errors.flatMap(_._2.flatMap(_.messages)) shouldBe List("invalid pageSize")
    }

    "invalid json when no resultCount provided" in {
      val exception = intercept[JsResultException] {
        Json
          .obj(
            "results"   -> Seq(""),
            "pageIndex" -> 1,
            "pageSize"  -> 1
          )
          .as[Paged[String]]
      }

      exception.errors.size                              shouldBe 1
      exception.errors.flatMap(_._2.flatMap(_.messages)) shouldBe List("invalid resultCount")
    }

    "json created from Paged model" in {
      val jsonCreated = Json.toJson(Paged(Seq(""), 1, 1, 1))
      jsonCreated shouldBe Json.obj(
        "results"     -> Json.arr(""),
        "pageIndex"   -> 1,
        "pageSize"    -> 1,
        "resultCount" -> 1
      )
    }

    "json created from Paged model when no values given" in {
      val jsonCreated = Json.toJson(Paged(Seq("")))
      jsonCreated shouldBe Json.obj(
        "results"     -> Json.arr(""),
        "pageIndex"   -> 1,
        "pageSize"    -> 100,
        "resultCount" -> 1
      )
    }

  }

}
