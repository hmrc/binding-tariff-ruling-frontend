/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

/*
 * Copyright 2019 HM Revenue & Customs
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

case class Paged[T](results: Seq[T], pageIndex: Int, pageSize: Int, resultCount: Int) {
  def map[X](f: T => X): Paged[X] = this.copy(results = results.map(f))
  def size: Int = results.size
  def pageCount: Int = Math.ceil(resultCount.toDouble / pageSize).toInt
  def isEmpty: Boolean = results.isEmpty
  def nonEmpty: Boolean = results.nonEmpty
}

object Paged {
  def empty[T]: Paged[T] = Paged(Seq.empty, 1, 0, 0)
  def empty[T](pagination: Pagination): Paged[T] = Paged(Seq.empty, pagination, 0)
  def apply[T](results: Seq[T], pagination: Pagination, resultCount: Int): Paged[T] = Paged(results, pagination.pageIndex, pagination.pageSize, resultCount)
  def apply[T](results: Seq[T]): Paged[T] = Paged(results, SimplePagination(), results.size)
  def apply[T](results: Seq[T], resultCount: Int): Paged[T] = Paged(results, SimplePagination(), resultCount)
}
