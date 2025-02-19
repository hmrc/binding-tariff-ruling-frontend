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

import org.mockito.BDDMockito.given
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.Call
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Paged
import uk.gov.hmrc.bindingtariffrulingfrontend.views.ViewMatchers.*
import uk.gov.hmrc.bindingtariffrulingfrontend.views.ViewSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.pagination

class PaginationViewSpec extends ViewSpec with BeforeAndAfterEach {

  private val goToPage: Int => Call = mock(classOf[Int => Call])

  override def beforeEach(): Unit = {

    def returnThePage: Answer[Call] =
      (invocation: InvocationOnMock) => Call(method = "GET", url = "/page=" + invocation.getArgument(0))

    super.beforeEach()
    when(goToPage.apply(ArgumentMatchers.any[Int])) will returnThePage
  }

  "Pagination" should {

    "Render empty page" in {

      val doc = view(
        pagination(
          id = "ID",
          pager = Paged(Seq.empty[String], pageIndex = 1, pageSize = 1, resultCount = 0),
          onChange = goToPage
        )
      )

      doc shouldNot containElementWithID("page-pagination")
    }

    "Render multiple pages" in {
      val doc = view(
        pagination(
          id = "ID",
          pager = Paged(Seq("", ""), pageIndex = 1, pageSize = 2, resultCount = 6),
          onChange = goToPage
        )
      )

      doc should containElementWithID("page-pagination")

      doc                                   should containElementWithID("ID-page_current")
      doc.getElementById("ID-page_current") should containText("1")

      doc                             should containElementWithID("ID-page_2")
      doc.getElementById("ID-page_2") should containElementWithAttribute("href", "/page=2")

      doc                             should containElementWithID("ID-page_3")
      doc.getElementById("ID-page_3") should containElementWithAttribute("href", "/page=3")

      doc                                should containElementWithID("ID-page_next")
      doc.getElementById("ID-page_next") should containElementWithAttribute("href", "/page=2")

    }
  }
}
