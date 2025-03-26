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
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.back_link
import uk.gov.hmrc.govukfrontend.views.html.components.GovukBackLink

class BackLinkViewSpec extends BaseSpec {

  "back_link template" should {

    "render the back link correctly" in {
      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq())

      val govukBackLink = app.injector.instanceOf[GovukBackLink]
      val backLinkView  = new back_link(govukBackLink)

      val html = backLinkView().toString()

      val doc: Document       = Jsoup.parse(html)
      val textContent: String = doc.text()

      textContent should include("Back")
    }

    "call render method directly" in {
      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq())

      val govukBackLink = app.injector.instanceOf[GovukBackLink]
      val backLinkView  = new back_link(govukBackLink)

      val renderHtml   = backLinkView.render(messages).toString()
      val expectedHtml = backLinkView.apply()(messages).toString()

      renderHtml shouldBe expectedHtml
    }

    "call template helper methods f and ref" in {
      val messagesApi                 = app.injector.instanceOf[MessagesApi]
      implicit val messages: Messages = messagesApi.preferred(Seq())

      val govukBackLink = app.injector.instanceOf[GovukBackLink]
      val backLinkView  = new back_link(govukBackLink)

      val functionCallHtml = backLinkView.f()(messages).toString()
      val expectedHtml     = backLinkView.apply()(messages).toString()

      val refCall = backLinkView.ref
      refCall shouldBe backLinkView
    }
  }
}
