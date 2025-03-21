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
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.{Lang, Messages, MessagesApi}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.error_summary

class ErrorSummaryViewSpec extends BaseSpec {
  "error_summary template" should {
    "not render error summary when form has no errors" in {
      // Create a form with no errors
      val form = Form(single("test" -> text())).bind(Map("test" -> "value"))

      // Get the messagesApi and create Messages
      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

      // Get the template
      val errorSummary = app.injector.instanceOf[error_summary]

      // Render the template
      val html = errorSummary(form).toString()

      // Should be effectively empty (might contain whitespace)
      html.trim shouldBe ""
    }

    "render error summary when form has errors" in {
      val form = Form(
        single("test" -> text().verifying("error.required", _.nonEmpty))
      ).bind(Map("test" -> ""))

      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

      val errorSummary = app.injector.instanceOf[error_summary]
      val html         = errorSummary(form).toString()

      // Parse the HTML to extract text only
      val doc: Document       = Jsoup.parse(html)
      val textContent: String = doc.text() // Extract only the visible text

      textContent should include("There's a problem This field is required")
    }

    "call render method directly" in {
      val form = Form(single("test" -> text().verifying("error.required", _.nonEmpty))).bind(Map("test" -> ""))

      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

      val errorSummary = app.injector.instanceOf[error_summary]

      // Explicitly call render to ensure coverage
      val html = errorSummary.render(form, messages).toString()

      // Extract only text from the rendered HTML
      val doc: Document       = Jsoup.parse(html)
      var textContent: String = doc.text()

      // Normalize apostrophes to prevent encoding issues
      textContent = textContent.replace("’", "'")

      // Verify expected text is present
      textContent should include("There's a problem") // Matches HTML-rendered text
      textContent should include("This field is required") // Matches actual error message
    }

    "call template helper methods f and ref" in {
      val form = Form(single("test" -> text().verifying("error.required", _.nonEmpty))).bind(Map("test" -> ""))

      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

      val errorSummary = app.injector.instanceOf[error_summary]

      // Ensure `f` behaves as expected
      val functionCall = errorSummary.f
      functionCall(form)(messages) shouldBe errorSummary.apply(form)(messages)

      // Ensure `ref` behaves as expected
      val refCall = errorSummary.ref
      refCall shouldBe errorSummary
    }
  }
}
