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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Pagination
import play.api.data.FormError
import play.api.data.format.Formatter

// scalastyle:off magic.number
case class SimpleSearch(
  query: Option[String],
  imagesOnly: Boolean,
  override val pageIndex: Int,
  override val pageSize: Int = 50
) extends Pagination
// scalastyle:on magic.number

object SimpleSearch {

  val form: Form[SimpleSearch] = Form(
    mapping(
      "query"  -> of(optionalStringFormatter),
      "images" -> boolean,
      "page"   -> optional(number).transform(_.getOrElse(1), (page: Int) => Some(page))
    )((q: Option[String], i: Boolean, p: Int) => SimpleSearch(q, i, p))(s => Some((s.query, s.imagesOnly, s.pageIndex)))
  )

  private def standardiseText(s: String): String =
    s.replaceAll("""\s{1,}""", " ").trim

  private lazy val optionalStringFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] =
      Right(
        data
          .get(key)
          .map(standardiseText)
          .filter(_.lengthCompare(0) > 0)
      )

    override def unbind(key: String, value: Option[String]): Map[String, String] =
      Map(key -> value.getOrElse(""))
  }

}
