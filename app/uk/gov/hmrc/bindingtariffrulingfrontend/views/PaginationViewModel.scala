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

package uk.gov.hmrc.bindingtariffrulingfrontend.views

import play.api.mvc.Call
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Paged
import uk.gov.hmrc.govukfrontend.views.viewmodels.pagination.{Pagination, PaginationItem, PaginationLink}

import javax.inject.Inject

class PaginationViewModel @Inject() ()() {

  def previous(call: Call): PaginationLink =
    PaginationLink(
      href       = call.url,
      text       = Some("Previous"),
      labelText  = Some("Previous"),
      attributes = Map()
    )

  def next(call: Call): PaginationLink =
    PaginationLink(
      href       = call.url,
      text       = Some("Next"),
      labelText  = Some("Next"),
      attributes = Map()
    )

  def items(start: Int, finish: Int, searchPageBaseUrl: Int => Call, pager: Paged[_]): Seq[PaginationItem] =

    (start to finish).foldLeft[Seq[PaginationItem]](Nil) { (acc, pageIndex) =>

      val anyPage = pageIndex == 0 || pageIndex == 1 || pageIndex <= pager.pageCount
      println(pager.pageCount)

      if (anyPage) {
        acc :+ PaginationItem(
          href    = searchPageBaseUrl(pageIndex).url,
          number  = Some(pageIndex.toString),
          current = Some(pageIndex == pager.pageIndex)
        )
      } else if (acc.lastOption.flatMap(_.ellipsis).contains(true)) {
        acc
      } else {
        acc :+ PaginationItem(ellipsis = Some(true))
      }
    }

  def pagination(searchPageBaseUrl: Int => Call, pager: Paged[_]): Pagination = {
    println(searchPageBaseUrl(pager.pageIndex).url)
    Pagination(
      Some(items(start = 1, finish = pager.pageCount, searchPageBaseUrl = searchPageBaseUrl, pager = pager)),
      Some(previous(searchPageBaseUrl(pager.pageIndex - 1))),
      Some(next(searchPageBaseUrl(pager.pageIndex + 1)))
    )
  }
}
