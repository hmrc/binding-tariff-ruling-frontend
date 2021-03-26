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
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.error

class ErrorViewSpec extends ViewSpec {

  val errorPage: error = app.injector.instanceOf[error]

  "Error View" should {
    "render error page with correct text" in {

      val doc =  view(errorPage("Title", "Heading", "Message"))

      doc.text() should include("Title")
      doc.text() should include("Heading")
      doc.text() should include("Message")
    }
  }
}