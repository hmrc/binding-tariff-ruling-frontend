/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.handlers

import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{DefaultAwaitTimeout, FakeRequest}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.ErrorHandler
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.error

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ErrorHandlerSpec
    extends AnyWordSpec
    with DefaultAwaitTimeout
    with Matchers
    with GuiceOneAppPerSuite
    with OptionValues {

  private val errorHandler      = app.injector.instanceOf[ErrorHandler]
  private val errorTemplateView = app.injector.instanceOf[error]

  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

  "ErrorHandler" should {

    "handle standardErrorTemplate" in {
      implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

      val result = Await.result(errorHandler.standardErrorTemplate("title", "heading", "msg test"), Duration.Inf)

      result mustBe errorTemplateView("title", "heading", "msg test")
    }

  }
}
