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


case class SimpleSearch
(
  query: String,
  override val pageIndex: Int,
  override val pageSize: Int = 50
) extends Pagination

object SimpleSearch {

  val form: Form[SimpleSearch] = Form(
    mapping(
      "query" -> nonEmptyText,
      "page" -> optional(number).transform(_.getOrElse(1), (page: Int) => Some(page))
    )((q: String, p: Int) => SimpleSearch(q, p))(s => Some((s.query, s.pageIndex)))
  )

}
