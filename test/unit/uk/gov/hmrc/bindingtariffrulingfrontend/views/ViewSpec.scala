/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.test.Helpers
import play.twirl.api.Html
import uk.gov.hmrc.bindingtariffrulingfrontend.UnitSpec

abstract class ViewSpec extends UnitSpec {

  protected def testMessages: Map[String, Map[String, String]]

  lazy val messagesApi: MessagesApi = new DefaultMessagesApi(testMessages)

  implicit lazy val messages: Messages = Helpers.stubMessages(messagesApi)

  protected def view(html: Html): Document =
    Jsoup.parse(html.toString())


}