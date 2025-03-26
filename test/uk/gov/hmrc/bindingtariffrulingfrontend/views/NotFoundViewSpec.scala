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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.mustBe
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import scala.concurrent.duration._
import play.api.test.FakeRequest
import org.apache.pekko.util.Timeout
import play.api.test.Helpers.{contentAsString, stubMessagesApi}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.not_found
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.template.main_template

class NotFoundViewSpec extends BaseSpec {

  private val messagesApi = app.injector.instanceOf[MessagesApi]

  "not_found template" should {
    val mainTemplate              = app.injector.instanceOf[main_template]
    val notFoundView              = new not_found(mainTemplate)
    implicit val timeout: Timeout = Timeout(5.seconds)
    "render the Not Found page with correct title and messages" in {
      implicit val request            = FakeRequest()
      implicit val messages: Messages = messagesApi.preferred(Seq())

      val html                = notFoundView().toString()
      val doc: Document       = Jsoup.parse(html)
      val textContent: String = doc.text()

      doc.title() should startWith("Page not found")

      doc.select("h1#not_found-heading").text() mustBe messages("site.notfound.heading")

      textContent should include(messages("site.notfound.checkcorrect"))
      textContent should include(messages("site.notfound.checkcomplete"))
    }

    "render the page correctly using render method" in {
      implicit val request: Request[_] = FakeRequest()
      implicit val messages: Messages  = stubMessagesApi().preferred(request)

      val renderedHtml = notFoundView.render(request, messages)
      val content      = contentAsString(renderedHtml)(timeout)

      content should include(messages("site.notfound.title"))
      content should include(messages("site.notfound.heading"))
    }

    "correctly use f method" in {
      val function                     = notFoundView.f
      implicit val request: Request[_] = FakeRequest()
      implicit val messages: Messages  = stubMessagesApi().preferred(request)

      val result = function()(request, messages)
      contentAsString(result)(timeout) should include(messages("site.notfound.title"))
    }

    "have a correct ref method" in {
      notFoundView.ref mustBe notFoundView
    }
  }
}
