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

package uk.gov.hmrc.bindingtariffrulingfrontend.views

import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling, SimplePagination}

import java.time.Instant

class ResultSummarySpec extends ViewSpec {

  val ruling: Ruling = Ruling(reference = "ref1", "0", Instant.now, Instant.now(), "justification", "exacting")

  override val testMessages: Map[String, Map[String, String]] = Map(
    "default" -> Map(
      "search.summary.nomatch"          -> "There are no rulings that match your search.",
      "search.summary.onematch"         -> "Showing 1 result that matches your search.",
      "search.summary.onepage"          -> "Showing {0} results that match your search.",
      "search.summary.manypage"         -> "Showing {0} to {1} of {2} results that match your search.",
      "search.summary.nomatch.landing"  -> "There are no rulings.",
      "search.summary.onematch.landing" -> "Showing 1 result.",
      "search.summary.onepage.landing"  -> "Showing {0} results.",
      "search.summary.manypage.landing" -> "Showing {0} to {1} of {2} results."
    )
  )

  "result summary" should {
    "render search results when there are no results for the search query" in {

      val renderedView = view(
        html.components.result_summary(
          "results",
          Paged.empty,
          "search.summary.onematch",
          "search.summary.onepage",
          "search.summary.manypage",
          "search.summary.nomatch"
        )
      )

      renderedView.text() should include("There are no rulings that match your search.")
    }

    "render search results when there is 1 result for the search query" in {

      val results = Paged(Seq(ruling))

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch",
          "search.summary.onepage",
          "search.summary.manypage",
          "search.summary.nomatch"
        )
      )

      renderedView.text() should include("Showing 1 result that matches your search.")
    }

    "render search results when there is 1 page of results for the search query" in {
      val collectionElements = 6
      val results            = Paged(Seq.fill(collectionElements)(ruling))

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch",
          "search.summary.onepage",
          "search.summary.manypage",
          "search.summary.nomatch"
        )
      )

      renderedView.text() should include("Showing 6 results that match your search.")
    }

    "render search results for first page when there are multiple pages" in {
      val pageIndex          = 1
      val pageSize           = 25
      val collectionElements = 25
      val resultCount        = 100

      val results = Paged(Seq.fill(collectionElements)(ruling), SimplePagination(pageIndex, pageSize), resultCount)

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch",
          "search.summary.onepage",
          "search.summary.manypage",
          "search.summary.nomatch"
        )
      )

      renderedView.text() should include("Showing 1 to 25 of 100 results that match your search.")
    }

    "render search results for second page when there are multiple pages" in {
      val pageIndex          = 2
      val pageSize           = 25
      val collectionElements = 25
      val resultCount        = 100

      val results = Paged(Seq.fill(collectionElements)(ruling), SimplePagination(pageIndex, pageSize), resultCount)

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch",
          "search.summary.onepage",
          "search.summary.manypage",
          "search.summary.nomatch"
        )
      )

      renderedView.text() should include("Showing 26 to 50 of 100 results that match your search.")
    }
  }

  "result summary when landing on the page for the first time" should {
    "render search results when there are no results" in {

      val renderedView = view(
        html.components.result_summary(
          "results",
          Paged.empty,
          "search.summary.onematch.landing",
          "search.summary.onepage.landing",
          "search.summary.manypage.landing",
          "search.summary.nomatch.landing"
        )
      )

      renderedView.text() should include("There are no rulings.")
    }

    "render search results when there is 1 result" in {

      val results = Paged(Seq(ruling))

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch.landing",
          "search.summary.onepage.landing",
          "search.summary.manypage.landing",
          "search.summary.nomatch.landing"
        )
      )

      renderedView.text() should include("Showing 1 result.")
    }

    "render search results when there is 1 page of results" in {

      val results = Paged(Seq.fill(6)(ruling))

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch.landing",
          "search.summary.onepage.landing",
          "search.summary.manypage.landing",
          "search.summary.nomatch.landing"
        )
      )

      renderedView.text() should include("Showing 6 results.")
    }

    "render search results for first page when there are multiple pages" in {
      val pageIndex          = 1
      val pageSize           = 25
      val collectionElements = 25
      val resultCount        = 100

      val results = Paged(Seq.fill(collectionElements)(ruling), SimplePagination(pageIndex, pageSize), resultCount)

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch.landing",
          "search.summary.onepage.landing",
          "search.summary.manypage.landing",
          "search.summary.nomatch.landing"
        )
      )

      renderedView.text() should include("Showing 1 to 25 of 100 results.")
    }

    "render search results for second page when there are multiple pages" in {

      val results = Paged(Seq.fill(25)(ruling), SimplePagination(2, 25), 100)

      val renderedView = view(
        html.components.result_summary(
          "results",
          results,
          "search.summary.onematch.landing",
          "search.summary.onepage.landing",
          "search.summary.manypage.landing",
          "search.summary.nomatch.landing"
        )
      )

      renderedView.text() should include("Showing 26 to 50 of 100 results.")
    }
  }

}
