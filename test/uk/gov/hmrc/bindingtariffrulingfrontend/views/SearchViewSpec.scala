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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.data.Form
import play.api.i18n.{Lang, Messages}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, stubMessagesApi}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.{error_summary, input_search, search_results}
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.search
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.template.main_template
import uk.gov.hmrc.govukfrontend.views.html.components.FormWithCSRF

import java.time.Instant
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
      "en" -> Map( // ✅ Corrected from "default" -> Map()
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
        "footer.accessibility.text"          -> "Accessibility",
        "footer.privacy.text"                -> "Privacy",
        "footer.termsConditions.text"        -> "Terms and Conditions",
        "footer.govukHelp.text"              -> "GOV.UK Help",
        "footer.contact.text"                -> "Contact",
        "footer.welshHelp.text"              -> "Welsh Help",
        "footer.content.licence.before.link" -> "Content licensed under",
        "footer.content.licence.link.text"   -> "Open Government Licence v3.0",
        "footer.content.licence.after.link"  -> "unless stated otherwise.",
        "footer.crown.copyright"             -> "© Crown Copyright"
      )
    )

//  "Search view" should {
//
//    "render correctly with no search results" in {
//      val renderedHtml  = searchView.render(form, None, fileMetadata, request, messages, appConfig)
//      val doc: Document = Jsoup.parse(contentAsString(renderedHtml))
//
//      doc.select("input[name=query]").`val`() mustBe ""
//      doc.text() should include("Search for Advance Tariff Rulings")
//      doc.text() should include("You can search using a combination of words, commodity codes and reference numbers.")
//    }
//
//    "render correctly when search results exist" in {
//      val searchResults = Some(
//        Paged(
//          Seq(
//            Ruling(
//              reference = "Test Ruling",
//              bindingCommodityCode = "123456",
//              effectiveStartDate = Instant.now(),
//              effectiveEndDate = Instant.now().plusSeconds(86400),
//              justification = "Some justification",
//              goodsDescription = "Some goods description",
//              keywords = Set("keyword1", "keyword2"),
//              attachments = Seq.empty,
//              images = Seq.empty
//            )
//          ),
//          1,
//          1,
//          1
//        )
//      )
//      val renderedHtml  = searchView.render(form, searchResults, fileMetadata, request, messages, appConfig)
//      val doc: Document = Jsoup.parse(contentAsString(renderedHtml))
//
//      doc.text() should include("Search for Advance Tariff Rulings")
//      doc.text() should include("1 result found")
//    }
//
//    "display error summary when form has errors" in {
//      val formWithError = form.bind(Map("query" -> ""))
//      val renderedHtml  = searchView.render(formWithError, None, fileMetadata, request, messages, appConfig)
//      val doc: Document = Jsoup.parse(contentAsString(renderedHtml))
//
//      doc.text() should include("There’s a problem")
//      doc.text() should include("Enter a search term")
//    }
//
//    "correctly use f method" in {
//      val function = searchView.f
//
//      implicit val request: Request[_] = FakeRequest()
//      implicit val messages: Messages  = stubMessagesApi().preferred(Seq(Lang("en"))) // ✅ Fixed
//
//      val renderedHtml  = function(form, None, fileMetadata)(request, messages, appConfig)
//      val doc: Document = Jsoup.parse(contentAsString(renderedHtml))
//
//      println("===== Rendered HTML Text =====")
//      println(doc.text())
//      println("==============================")
//
//      doc.text().trim.toLowerCase should include("search for advance tariff rulings") // ✅ Normalized check
//    }
//
//    "correctly reference ref method" in {
//      searchView.ref mustBe searchView
//    }
//  }

}
