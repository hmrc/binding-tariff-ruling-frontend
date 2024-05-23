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

package uk.gov.hmrc.bindingtariffrulingfrontend.views

import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}

import java.time.Instant

class PaginationSpec extends ViewSpec {

  val fakeRuling: Ruling =
    Ruling(
      reference            = "reference",
      bindingCommodityCode = "bindingCommodityCode",
      effectiveStartDate   = Instant.now,
      effectiveEndDate     = Instant.now,
      justification        = "justification",
      goodsDescription     = "goodsDescription",
      keywords             = Set("k1", "k2", "k3"),
      attachments          = Seq("f1", "f2", "f3"),
      images               = Seq("id1", "id2")
    )

  val searches: Option[Paged[Ruling]] = Some(Paged(Seq(fakeRuling, fakeRuling, fakeRuling)))

  val paginationViewModel: PaginationViewModel = new PaginationViewModel(searches)

  "PaginationSpec" when {

    "given 0 rulings" should {

      "return " in {
        val actual   = paginationViewModel.next
        val expected =
      }
    }
  }
}
