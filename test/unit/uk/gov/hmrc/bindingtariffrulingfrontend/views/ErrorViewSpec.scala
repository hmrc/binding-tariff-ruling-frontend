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
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.error

class ErrorViewSpec extends ViewSpec {

  private val page = app.injector.instanceOf[error]
  val viewViaApply: () => HtmlFormat.Appendable =
    () => page("Title", "Heading", "Message")(FakeRequest(), messages)
  val viewViaRender: () => HtmlFormat.Appendable =
    () => page.render("Title", "Heading", "Message", FakeRequest(), messages)
  val viewViaF: () => HtmlFormat.Appendable =
    () => page.f("Title", "Heading", "Message")(FakeRequest(), messages)

  "ErrorView" should {
    def test(method: String, viewMethod: () => HtmlFormat.Appendable): Unit =
      s"$method" in {
        val doc = view(viewMethod())

        doc.text() should include("Title")
        doc.text() should include("Heading")
        doc.text() should include("Message")
      }

    val input: Seq[(String, () => HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => (test _).tupled(args))
  }
}
