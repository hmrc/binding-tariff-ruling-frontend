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

package uk.gov.hmrc.bindingtariffrulingfrontend.views

import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling, SimplePagination}

import java.time.Instant

class ResultSummarySpec extends ViewSpec {

  val ruling = Ruling(reference = "ref1", "0", Instant.now, Instant.now(), "justification", "exacting")

  override val testMessages = Map(
    "default" -> Map(
      "search.summary.nomatch"  -> "There are no rulings that match your search.",
      "search.summary.onematch" -> "Showing 1 result that matches your search.",
      "search.summary.onepage"  -> "Showing {0} results that match your search.",
      "search.summary.manypage" -> "Showing {0} to {1} of {2} results that match your search."
    )
  )

  "result summary" should {
    "render search results when there are no results for the search query" in {

      val renderedView = view(html.components.result_summary("results", Paged.empty))

      renderedView.text() should include("There are no rulings that match your search.")
    }

    "render search results when there is 1 result for the search query" in {

      val results = Paged(Seq(ruling))

      val renderedView = view(html.components.result_summary("results", results))

      renderedView.text() should include("Showing 1 result that matches your search.")
    }

    "render search results when there is 1 page of results for the search query" in {

      val results = Paged(Seq.fill(6)(ruling))

      val renderedView = view(html.components.result_summary("results", results))

      renderedView.text() should include("Showing 6 results that match your search.")
    }

    "render search results for first page when there are multiple pages" in {

      val results = Paged(Seq.fill(25)(ruling), SimplePagination(1, 25), 100)

      val renderedView = view(html.components.result_summary("results", results))

      renderedView.text() should include("Showing 1 to 25 of 100 results that match your search.")
    }

    "render search results for second page when there are multiple pages" in {

      val results = Paged(Seq.fill(25)(ruling), SimplePagination(2, 25), 100)

      val renderedView = view(html.components.result_summary("results", results))

      renderedView.text() should include("Showing 26 to 50 of 100 results that match your search.")
    }
  }
}
