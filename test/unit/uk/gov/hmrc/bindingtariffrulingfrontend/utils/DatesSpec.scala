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

package uk.gov.hmrc.bindingtariffrulingfrontend.utils

import java.time.{LocalDate, ZoneOffset}

import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class DatesSpec extends BaseSpec {

  "Format" should {

    "convert instant to string" in {
      val year   = 2018
      val month  = 1
      val day    = 1
      val date   = LocalDate.of(year, month, day).atStartOfDay(ZoneOffset.UTC).toInstant
      val output = Dates.format(date)

      output shouldBe "01 Jan 2018"
    }

  }

}
