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
import org.mockito.Mockito.mock
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.bindingtariffrulingfrontend.utils.Dates
import uk.gov.hmrc.bindingtariffrulingfrontend.views.ViewMatchers._
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.ruling

import java.time.Instant

class RulingViewSpec extends ViewSpec {

  override implicit val appConfig: AppConfig = mock(classOf[AppConfig])

  override protected val testMessages: Map[String, Map[String, String]] =
    Map(
      "default" -> Map(
        "search.results.ruling.commodityCode.newTab" -> "(opens in new tab)",
        "ruling.title"                               -> "Information for ruling {0}",
        "ruling.heading"                             -> "Information for ruling {0}",
        "site.heading.secondary"                     -> "Advance Tariff Ruling",
        "ruling.printRuling"                         -> "Print this ruling",
        "ruling.startDate"                           -> "Start date",
        "ruling.expiryDate"                          -> "Expiry date",
        "ruling.commodityCode"                       -> "Commodity code",
        "ruling.description"                         -> "Description",
        "ruling.keywords"                            -> "Keywords",
        "ruling.images"                              -> "Images",
        "ruling.attachments"                         -> "Attachments",
        "ruling.justification"                       -> "Justification"
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
    "id1" -> FileMetadata("id1", Some("image1"), None, None),
    "id2" -> FileMetadata("id2", Some("image1"), None, None),
    "f1"  -> FileMetadata("f1", Some("file1"), None, None),
    "f2"  -> FileMetadata("f2", Some("file2"), None, None)
  )

  val page: ruling = app.injector.instanceOf[ruling]

  val viewViaApply: () => HtmlFormat.Appendable =
    () => page(ruling, fileMetaData)(FakeRequest(), messages, appConfig)
  val viewViaRender: () => HtmlFormat.Appendable =
    () => page.render(ruling, fileMetaData, FakeRequest(), messages, appConfig)
  val viewViaF: () => HtmlFormat.Appendable =
    () => page.f(ruling, fileMetaData)(FakeRequest(), messages, appConfig)

  "Ruling View" should {
    def test(method: String, viewMethod: () => HtmlFormat.Appendable): Unit =
      s"$method" when {
        "render with correct title, heading and fields" in {

          val doc = view(viewMethod())

          doc.text() should include(s"${ruling.bindingCommodityCode} (opens in new tab)")
          doc.text() should include(s"Information for ruling ${ruling.reference}")
          doc.text() should include(s"Information for ruling ${ruling.reference}")
          doc.text() should include(s"Advance Tariff Ruling")
          doc.text() should include(s"Print this ruling")

          doc.text() should include(s"Start date")
          doc.text() should include(Dates.format(ruling.effectiveStartDate))

          doc.text() should include(s"Expiry date")
          doc.text() should include(Dates.format(ruling.effectiveEndDate))

          doc.text() should include(s"Commodity code")
          doc.text() should include(s"${ruling.bindingCommodityCode} (opens in new tab)")

          doc.text() should include(s"Description")
          doc.text() should include(s"${ruling.goodsDescription}")

          doc.text() should include(s"Keywords")
          ruling.keywords.map(keyword => doc.text() should include(keyword))

          doc.text() should include(s"Justification")
          doc.text() should include(s"${ruling.justification}")
        }

        "render images with correct href (images toggle on)" in {

          given(appConfig.displayImages) willReturn true

          val doc = view(viewMethod())

          doc.text() should include(s"Images")
          doc.getElementById("ruling-details") should containElementWithAttribute(
            "href",
            s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id1"
          )
          doc.getElementById("ruling-details") should containElementWithAttribute(
            "href",
            s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id2"
          )
        }

        "do not render images with correct href (images toggle off)" in {

          given(appConfig.displayImages) willReturn false

          val doc = view(viewMethod())

          doc.text() shouldNot include(s"Images")
          doc.getElementById("ruling-details") shouldNot containElementWithAttribute(
            "href",
            s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id1"
          )
          doc.getElementById("ruling-details") shouldNot containElementWithAttribute(
            "href",
            s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/image/id2"
          )
        }

        "render attachments with correct href" in {

          val doc = view(viewMethod())

          doc.text() should include(s"Attachments")
          doc.getElementById("ruling-details") should containElementWithAttribute(
            "href",
            s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/attachment/f1"
          )
          doc.getElementById("ruling-details") should containElementWithAttribute(
            "href",
            s"/search-for-advance-tariff-rulings/ruling/${ruling.reference}/attachment/f2"
          )
        }

      }

    val input: Seq[(String, () => HtmlFormat.Appendable)] = Seq(
      (".apply", viewViaApply),
      (".render", viewViaRender),
      (".f", viewViaF)
    )

    input.foreach(args => (test _).tupled(args))

  }
}
