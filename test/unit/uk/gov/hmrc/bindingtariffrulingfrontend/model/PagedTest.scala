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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.play.test.UnitSpec

class PagedTest extends UnitSpec {

  "Paged" should {
    val search = SimpleSearch

    "map" in {
      Paged(Seq("hello")).map(_.toUpperCase) shouldBe Paged(Seq("HELLO"))
    }

    "calculate size" in {
      Paged.empty.size shouldBe 0
      Paged(Seq("")).size shouldBe 1
    }

    "calculate isEmpty" in {
      Paged.empty.isEmpty shouldBe true
      Paged(Seq("")).isEmpty shouldBe false
    }

    "calculate pageCount" in {
      Paged.empty.pageCount shouldBe 0
      Paged(results = Seq(), pageIndex = 1, pageSize = 1, resultCount = 1).pageCount shouldBe 1
      Paged(results = Seq(), pageIndex = 1, pageSize = 1, resultCount = 2).pageCount shouldBe 2
      Paged(results = Seq(), pageIndex = 1, pageSize = 2, resultCount = 3).pageCount shouldBe 2
      Paged(results = Seq(), pageIndex = 1, pageSize = 10, resultCount = 100).pageCount shouldBe 10
      Paged(results = Seq(), pageIndex = 1, pageSize = 1, resultCount = 100).pageCount shouldBe 100
    }

    "calculate nonEmpty" in {
      Paged.empty.nonEmpty shouldBe false
      Paged(Seq("")).nonEmpty shouldBe true
    }
  }

}
