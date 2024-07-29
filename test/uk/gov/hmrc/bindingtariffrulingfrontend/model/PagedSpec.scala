/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, JsResultException, Json}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class PagedSpec extends BaseSpec {

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

    "calculate pageCount" in {
      Paged.empty.pageCount                                                             shouldBe 0
      Paged(results = Seq(), pageIndex = 1, pageSize = 1, resultCount = 1).pageCount    shouldBe 1
      Paged(results = Seq(), pageIndex = 1, pageSize = 1, resultCount = 2).pageCount    shouldBe 2
      Paged(results = Seq(), pageIndex = 1, pageSize = 2, resultCount = 3).pageCount    shouldBe 2
      Paged(results = Seq(), pageIndex = 1, pageSize = 10, resultCount = 100).pageCount shouldBe 10
      Paged(results = Seq(), pageIndex = 1, pageSize = 1, resultCount = 100).pageCount  shouldBe 100
      Paged(results = Seq(), resultCount = 1).pageCount                                 shouldBe 1
      Paged(results = Seq(), resultCount = 100).pageCount                               shouldBe 1
      Paged(results = Seq(), resultCount = 1000).pageCount                              shouldBe 10
    }

    "calculate nonEmpty" in {
      Paged.empty.nonEmpty                     shouldBe false
      Paged.empty(SimplePagination()).nonEmpty shouldBe false
      Paged(Seq("")).nonEmpty                  shouldBe true
    }

    "parse valid json to paged object" in {
      val validPaged         = buildPagedJson(Seq(""), 1, 1, 1).as[Paged[String]]
      val expectedPageResult = Paged(Seq(""), 1, 1, 1)
      validPaged shouldBe expectedPageResult
    }

    Seq("results", "pageIndex", "pageSize", "resultCount").foreach { key =>
      s"raise exception when parsing json missing $key value" in {
        val exception = intercept[JsResultException] {
          (buildPagedJson - key).as[Paged[String]]
        }

        exception.errors.flatMap(_._2.flatMap(_.messages)) shouldBe List(s"invalid $key")
      }
    }

    "json created from Paged model" in {
      val jsonCreated  = Json.toJson(Paged(Seq(""), 1, 1, 1))
      val expectedJson = buildPagedJson(Seq(""), 1, 1, 1)
      jsonCreated shouldBe expectedJson
    }

    "json created from Paged model when no values given" in {
      val jsonCreated  = Json.toJson(Paged(Seq("")))
      val expectedJson = buildPagedJson(Seq(""), 1, 100, 1)
      jsonCreated shouldBe expectedJson
    }

  }
  private def buildPagedJson: JsObject =
    buildPagedJson(Seq(""), 1, 1, 1)
  private def buildPagedJson(results: Seq[String], pageIndex: Int, pageSize: Int, resultCount: Long): JsObject =
    Json.obj("results" -> results, "pageIndex" -> pageIndex, "pageSize" -> pageSize, "resultCount" -> resultCount)
}
