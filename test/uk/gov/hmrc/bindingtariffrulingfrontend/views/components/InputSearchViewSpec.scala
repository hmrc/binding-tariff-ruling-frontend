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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.input_search
import uk.gov.hmrc.govukfrontend.views.html.components.*

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
      val form          = SimpleSearch.form.fill(SimpleSearch(Some("query"), imagesOnly = false, 1))
      val html          = inputSearchView(form).toString()
      val doc: Document = Jsoup.parse(html)
      val inputElement  = doc.select("input[name=query]")
      val inputValue    = inputElement.attr("value")
      inputValue shouldBe "query"
      doc.text()   should include("Search for Advance Tariff Rulings")
      doc.text()   should include("Only show rulings that include images")
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

      renderHtml shouldBe expectedHtml
    }

    "correctly render the search form using f method" in {
      val renderFunction = inputSearchView.f
      val renderedHtml   = renderFunction(form)(messages, appConfig).toString()

      val doc: Document       = Jsoup.parse(renderedHtml)
      val textContent: String = doc.text()

      val queryInputValue = doc.select("input[name=query]").`val`()

      queryInputValue shouldBe "query"
      textContent       should include("Search for Advance Tariff Rulings")
    }

    "correctly reference the component with ref method" in {
      inputSearchView.ref shouldBe inputSearchView
    }

  }
}
