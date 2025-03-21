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

package uk.gov.hmrc.bindingtariffrulingfrontend.views.components

import org.apache.pekko.util.Timeout
import scala.concurrent.duration._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.govukfrontend.views.html.components.*
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.input_search

class InputSearchViewSpec extends BaseSpec {

  "input_search template" should {
    val messagesApi                 = app.injector.instanceOf[MessagesApi]
    implicit val messages: Messages = messagesApi.preferred(Seq())

    implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

    val govukLabel      = app.injector.instanceOf[GovukLabel]
    val govukHint       = app.injector.instanceOf[GovukHint]
    val govukInput      = app.injector.instanceOf[GovukInput]
    val govukButton     = app.injector.instanceOf[GovukButton]
    val govukCheckBoxes = app.injector.instanceOf[GovukCheckboxes]

    val inputSearchView = new input_search(govukLabel, govukHint, govukInput, govukButton, govukCheckBoxes)

    val form = SimpleSearch.form.fill(SimpleSearch(Some("query"), imagesOnly = false, 1))

    "render input search with pre-filled query" in {
      // Step 1: Fill the form with a search query
      val form = SimpleSearch.form.fill(SimpleSearch(Some("query"), imagesOnly = false, 1))

      // Step 2: Render the input_search component
      val html          = inputSearchView(form).toString()
      val doc: Document = Jsoup.parse(html)

      // Debugging: Print the entire HTML output
      println("Rendered HTML:\n" + doc.toString())

      // Step 3: Extract the input field and check its value
      val inputElement = doc.select("input[name=query]") // Ensure the selector matches your input field
      val inputValue   = inputElement.attr("value") // Extract the value attribute

      // Debugging: Print the extracted input value
      println("Extracted input value: " + inputValue)

      // Step 4: Assertions
      inputValue shouldBe "query" // Ensure the input field has the correct value
      doc.text()   should include("Search for Advance Tariff Rulings") // Ensure title is present
      doc.text()   should include("Only show rulings that include images") // Check presence of form labels
    }

    "call render method directly" in {
      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq())

      implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig] // ✅ Explicit type added

      val govukLabel      = app.injector.instanceOf[GovukLabel]
      val govukHint       = app.injector.instanceOf[GovukHint]
      val govukInput      = app.injector.instanceOf[GovukInput]
      val govukButton     = app.injector.instanceOf[GovukButton]
      val govukCheckBoxes = app.injector.instanceOf[GovukCheckboxes]

      val inputSearchView = new input_search(govukLabel, govukHint, govukInput, govukButton, govukCheckBoxes)

      val form = SimpleSearch.form.fill(SimpleSearch(Some("query"), imagesOnly = false, 1))

      val renderHtml   = inputSearchView.render(form, messages, appConfig).toString()
      val expectedHtml = inputSearchView.apply(form)(messages, appConfig).toString()

      renderHtml shouldBe expectedHtml // Ensures `render` is covered
    }

    "correctly render the search form using f method" in {
      val renderFunction = inputSearchView.f
      val renderedHtml   = renderFunction(form)(messages, appConfig).toString()

      val doc: Document       = Jsoup.parse(renderedHtml)
      val textContent: String = doc.text()

      // ✅ Extract the input field's value manually
      val queryInputValue = doc.select("input[name=query]").`val`() // Extract input value

      // Debugging Output
      println("\n===== Extracted Text Content =====\n" + textContent + "\n==============================\n")
      println(s"\n===== Extracted Query Input Value =====\n$queryInputValue\n==============================\n")

      // ✅ Verify that the query value is correctly set in the input field
      queryInputValue shouldBe "query"

      // ✅ Continue verifying expected text
      textContent should include("Search for Advance Tariff Rulings")
    }

    "correctly reference the component with ref method" in {
      inputSearchView.ref shouldBe inputSearchView
    }

  }
}
