/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.i18n.{DefaultMessagesApi, Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import uk.gov.hmrc.bindingtariffrulingfrontend.UnitSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

import scala.concurrent.Future

abstract class ViewSpec extends UnitSpec with BaseSpec {

  protected def testMessages: Map[String, Map[String, String]] = Map.empty

  lazy val messagesApi: MessagesApi = new DefaultMessagesApi(testMessages)

  implicit lazy val messages: Messages = Helpers.stubMessages(messagesApi)

  implicit val appConfig: AppConfig = realConfig

  implicit val fakeRequest  = FakeRequest()
/*

  protected def getRequestWithCSRF(uri: String = "/"): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", uri).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]
*/


  protected def view(html: Html): Document =
    Jsoup.parse(html.toString())

}
