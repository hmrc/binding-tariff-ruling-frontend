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

import org.apache.pekko.util.Timeout
import play.api.data.Form
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.{error_summary, input_search, search_results}
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.search
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.template.main_template
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF

import scala.concurrent.duration.*

class SearchViewSpec extends ViewSpec {

  implicit val timeout: Timeout = Timeout(5.seconds)

  implicit val request: Request[_] = FakeRequest()

  val mainTemplate      = app.injector.instanceOf[main_template]
  val errorSummary      = app.injector.instanceOf[error_summary]
  val searchInput       = app.injector.instanceOf[input_search]
  val searchResultsView = app.injector.instanceOf[search_results]
  val formWithCSRF      = app.injector.instanceOf[FormWithCSRF]

  val searchView = new search(mainTemplate, errorSummary, searchInput, searchResultsView, formWithCSRF)

  val form: Form[SimpleSearch]                = SimpleSearch.form
  val pagedResults: Option[Paged[Ruling]]     = None
  val fileMetadata: Map[String, FileMetadata] = Map.empty

  override protected def testMessages: Map[String, Map[String, String]] =
    Map(
      "en" -> Map(
        "search.summary.nomatch.landing"  -> "There are no rulings.",
        "search.summary.onematch.landing" -> "Showing 1 result.",
        "search.summary.onepage.landing"  -> "Showing {0} results.",
        "search.summary.manypage.landing" -> "Showing {0} to {1} of {2} results.",
        "search.heading"                  -> "Search for Advance Tariff Rulings",
        "search.hint"          -> "You can search using a combination of words, commodity codes and reference numbers.",
        "search.form.images"   -> "Only show rulings that include images",
        "search.form.submit"   -> "Search",
        "footer.support.links" -> "Support",
        "footer.cookies.text"  -> "Cookies",
        "footer.accessibility.text"   -> "Accessibility",
        "footer.privacy.text"         -> "Privacy",
        "footer.termsConditions.text" -> "Terms and Conditions",
        "footer.govukHelp.text"       -> "GOV.UK Help"
      )
    )

}
