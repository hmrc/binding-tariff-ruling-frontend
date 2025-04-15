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

import org.mockito.BDDMockito.`given`
import org.mockito.Mockito.{mock, when}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.utils.Dates
import uk.gov.hmrc.bindingtariffrulingfrontend.views.ViewMatchers.*
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.components.search_results

import java.time.Instant

class SearchResultsViewSpec extends ViewSpec {

  override implicit val appConfig: AppConfig = mock(classOf[AppConfig])

  override protected val testMessages: Map[String, Map[String, String]] =
    Map(
      "default" -> Map(
        "search.results.ruling.commodityCode.newTab" -> "(opens in new tab)",
        "search.results.ruling.description"          -> "Description",
        "search.results.ruling.keywords"             -> "Keywords",
        "search.results.ruling.expiryDate"           -> "Expiry date",
        "search.results.ruling.commodityCode"        -> "Commodity code",
        "search.results.ruling.images"               -> "Images",
        "search.results.ruling.view"                 -> "View ruling {0}"
      )
    )

  val ruling: Ruling = Ruling(
    reference = "reference",
    bindingCommodityCode = "bindingCommodityCode",
    effectiveStartDate = Instant.now,
    effectiveEndDate = Instant.now,
    justification = "justification",
    goodsDescription = "goodsDescription",
    keywords = Set("k1", "k2", "k3"),
    attachments = Seq("f1", "f2", "f3"),
    images = Seq("id1", "id2")
  )

  val fileMetaData: Map[String, FileMetadata] = Map(
    "id1" -> FileMetadata("id1", Some("image1"), None, Some("url"), published = true),
    "id2" -> FileMetadata("id2", Some("image1"), None, Some("url"), published = true),
    "f1"  -> FileMetadata("f1", Some("file1"), None, None),
    "f2"  -> FileMetadata("f2", Some("file2"), None, None)
  )

  val pagedRuling: Paged[Ruling] = Paged(Seq(ruling))

  val searchResultsView: search_results = app.injector.instanceOf[search_results]

  "Search Results View" should {

    "render correct text fields" in {

      val doc = view(searchResultsView(None, pagedRuling, fileMetaData))

      doc.text() should include(s"${ruling.bindingCommodityCode} (opens in new tab)")

      doc.text() should include(s"Expiry date")
      doc.text() should include(Dates.format(ruling.effectiveEndDate))

      doc.text() should include(s"Commodity code")
      doc.text() should include(s"${ruling.bindingCommodityCode} (opens in new tab)")

      doc.text() should include(s"Description")
      doc.text() should include(s"${ruling.goodsDescription}")

      doc.text() should include(s"Keywords")
      ruling.keywords.map(keyword => doc.text() should include(keyword))
    }

    "render images with correct href (toggle images on)" in {

      when(appConfig.displayImages).thenReturn(true)

      val doc = view(searchResultsView(None, pagedRuling, fileMetaData))

      doc.text() should include(s"Images")
      doc.getElementById("search_results-list-0") should containElementWithAttribute(
        "href",
        s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id1"
      )
      doc.getElementById("search_results-list-0") should containElementWithAttribute(
        "href",
        s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id2"
      )
    }

    "render images with correct href (toggle images off)" in {

      when(appConfig.displayImages).thenReturn(false)

      val doc = view(searchResultsView(None, pagedRuling, fileMetaData))

      doc.text() shouldNot include(s"Images")
      doc.getElementById("search_results-list-0") shouldNot containElementWithAttribute(
        "href",
        s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id1"
      )
      doc.getElementById("search_results-list-0") shouldNot containElementWithAttribute(
        "href",
        s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id2"
      )
    }

    "render with correct href link" in {

      val doc = view(searchResultsView(None, pagedRuling, fileMetaData))

      doc.text() should include(s"View ruling ${ruling.reference}")
      doc.getElementById("search_results-list-0") should containElementWithAttribute(
        "href",
        s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}"
      )
    }
  }
}
