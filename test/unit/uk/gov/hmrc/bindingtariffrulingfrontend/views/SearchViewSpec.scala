/*
 * Copyright 2023 HM Revenue & Customs
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

class SearchViewSpec extends ViewSpec {

  override protected def testMessages: Map[String, Map[String, String]] =
    Map(
      "default" -> Map(
        "search.summary.nomatch.landing"  -> "There are no rulings.",
        "search.summary.onematch.landing" -> "Showing 1 result.",
        "search.summary.onepage.landing"  -> "Showing {0} results.",
        "search.summary.manypage.landing" -> "Showing {0} to {1} of {2} results."
      )
    )
}
