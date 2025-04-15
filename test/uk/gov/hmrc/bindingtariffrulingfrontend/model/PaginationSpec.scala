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

import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class PaginationSpec extends BaseSpec {

  "Pagination" when {
    "withPage" should {
      "update pageIndex for SimplePagination" in {
        val pagination    = SimplePagination()
        val newPagination = pagination.withPage(2)
        newPagination.pageIndex should be(2)
        newPagination.pageSize  should be(100)
      }

      "update pageIndex for NoPagination" in {
        val pagination    = NoPagination(1, Integer.MAX_VALUE)
        val newPagination = pagination.withPage(2)
        newPagination.pageIndex should be(2)
        newPagination.pageSize  should be(Integer.MAX_VALUE)
      }

      "return NoPagination with default values for unknown implementations" in {
        val unknownPagination = new Pagination {
          val pageIndex: Int = 1
          val pageSize: Int  = 10
        }
        val newPagination = unknownPagination.withPage(2)
        newPagination         shouldBe a[NoPagination]
        newPagination.pageIndex should be(1)
        newPagination.pageSize  should be(Integer.MAX_VALUE)
      }
    }

    "NoPagination" should {
      "be created with default values" in {
        val pagination = NoPagination()
        pagination.pageIndex shouldBe 1
        pagination.pageSize  shouldBe Integer.MAX_VALUE
      }

      "be created with custom values" in {
        val pagination = NoPagination(2, 100)
        pagination.pageIndex shouldBe 2
        pagination.pageSize  shouldBe 100
      }
    }
  }
}
