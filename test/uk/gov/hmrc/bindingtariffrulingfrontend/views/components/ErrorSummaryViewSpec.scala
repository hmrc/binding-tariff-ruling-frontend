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
      val form = Form(single("test" -> text())).bind(Map("test" -> "value"))

      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

      val errorSummary = app.injector.instanceOf[error_summary]

      val html = errorSummary(form).toString()

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

      val doc: Document       = Jsoup.parse(html)
      val textContent: String = doc.text()

      textContent should include("There's a problem This field is required")
    }

    "call render method directly" in {
      val form = Form(single("test" -> text().verifying("error.required", _.nonEmpty))).bind(Map("test" -> ""))

      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

      val errorSummary = app.injector.instanceOf[error_summary]

      val html = errorSummary.render(form, messages).toString()

      val doc: Document       = Jsoup.parse(html)
      var textContent: String = doc.text()

      textContent = textContent.replace("’", "'")

      textContent should include("There's a problem")
      textContent should include("This field is required")
    }

    "call template helper methods f and ref" in {
      val form = Form(single("test" -> text().verifying("error.required", _.nonEmpty))).bind(Map("test" -> ""))

      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq(Lang("en")))

      val errorSummary = app.injector.instanceOf[error_summary]

      val functionCall = errorSummary.f
      functionCall(form)(messages) shouldBe errorSummary.apply(form)(messages)

      val refCall = errorSummary.ref
      refCall shouldBe errorSummary
    }
  }
}
