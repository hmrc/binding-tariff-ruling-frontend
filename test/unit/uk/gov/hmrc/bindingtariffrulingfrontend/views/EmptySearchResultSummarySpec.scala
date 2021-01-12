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

package uk.gov.hmrc.bindingtariffrulingfrontend.views

import java.time.Instant

import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling, SimplePagination}

class EmptySearchResultSummarySpec extends ViewSpec {

  val ruling = Ruling(reference = "ref1", "0", Instant.now, Instant.now(), "justification", "exacting")

  override val testMessages = Map(
    "default" -> Map(
      "search.summary.nomatch.landing"  -> "There are no rulings.",
      "search.summary.onematch.landing" -> "Showing 1 result.",
      "search.summary.onepage.landing"  -> "Showing {0} results.",
      "search.summary.manypage.landing" -> "Showing {0} to {1} of {2} results."
    )
  )

  "empty search result summary" should {
    "render search results when there are no results" in {

      val renderedView = view(html.components.empty_search_result_summary("results", Paged.empty, SimpleSearch.landingForm))

      renderedView.text() should include("There are no rulings.")
    }

    "render search results when there is 1 result" in {

      val results = Paged(Seq(ruling))

      val renderedView = view(html.components.empty_search_result_summary("results", results, SimpleSearch.landingForm))

      renderedView.text() should include("Showing 1 result.")
    }

    "render search results when there is 1 page of results" in {

      val results = Paged(Seq.fill(6)(ruling))

      val renderedView = view(html.components.empty_search_result_summary("results", results, SimpleSearch.landingForm))

      renderedView.text() should include("Showing 6 results.")
    }

    "render search results for first page when there are multiple pages" in {

      val results = Paged(Seq.fill(25)(ruling), SimplePagination(1, 25), 100)

      val renderedView = view(html.components.empty_search_result_summary("results", results, SimpleSearch.landingForm))

      renderedView.text() should include("Showing 1 to 25 of 100 results.")
    }

    "render search results for second page when there are multiple pages" in {

      val results = Paged(Seq.fill(25)(ruling), SimplePagination(2, 25), 100)

      val renderedView = view(html.components.empty_search_result_summary("results", results, SimpleSearch.landingForm))

      renderedView.text() should include("Showing 26 to 50 of 100 results.")
    }
  }
}
