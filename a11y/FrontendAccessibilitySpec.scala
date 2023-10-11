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

import org.scalacheck.Arbitrary
import play.api.data.Form
import play.twirl.api.Html
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model._
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.bindingtariffrulingfrontend.views.html.template.main_template
import uk.gov.hmrc.scalatestaccessibilitylinter.views.AutomaticAccessibilitySpec

import java.time.Instant

class FrontendAccessibilitySpec extends AutomaticAccessibilitySpec {

  // Some view template parameters can't be completely arbitrary,
  // but need to have same values for pages to render properly.
  // eg. if there is validation or conditional logic in the twirl template.
  // These can be provided by calling `fixed()` to wrap an existing concrete value.
  private val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private val fileMetaData: Map[String, FileMetadata] = Map(
    "id1" -> FileMetadata("id1", Some("image1"), None, Some("url"), published = true),
    "id2" -> FileMetadata("id2", Some("image1"), None, Some("url"), published = true),
    "f1" -> FileMetadata("f1", Some("file1"), None, None),
    "f2" -> FileMetadata("f2", Some("file2"), None, None)
  )

  private val ruling: Ruling = Ruling(
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

  private val pagedRuling: Paged[Ruling] = Paged(Seq(ruling))

  override implicit val arbAsciiString: Arbitrary[String] = fixed("/")

  implicit val arbConfig: Arbitrary[AppConfig] = fixed(appConfig)

  implicit val arbPagedRuling: Arbitrary[Paged[Ruling]] = fixed(pagedRuling)

  implicit val arbFileMetaData: Arbitrary[Map[String, FileMetadata]] = fixed(fileMetaData)

  implicit val arbInputSearch: Arbitrary[Form[SimpleSearch]] = fixed(SimpleSearch.form)

  // This is the package where the page templates are located in your service
  val viewPackageName: String = "uk.gov.hmrc.bindingtariffrulingfrontend.views.html"

  // This is the layout class or classes which are injected into all full pages in your service.
  // This might be `HmrcLayout` or some custom class(es) that your service uses as base page templates.
  val layoutClasses: Seq[Class[main_template]] = Seq(classOf[main_template])

  // this partial function wires up the generic render() functions with arbitrary instances of the correct types.
  // Important: there's a known issue with intellij incorrectly displaying warnings here, you should be able to ignore these for now.
  override def renderViewByClass: PartialFunction[Any, Html] = {
    case error: views.html.error => render(error)
    case image: views.html.image => render(image)
    case notFound: views.html.not_found => render(notFound)
    case rulingView: views.html.ruling => render(rulingView)
    case search: views.html.search => render(search)
  }

  runAccessibilityTests()
}
