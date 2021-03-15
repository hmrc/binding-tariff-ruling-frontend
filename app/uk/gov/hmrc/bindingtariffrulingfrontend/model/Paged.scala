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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

import play.api.libs.json.{Format, JsArray, JsDefined, JsError, JsNumber, JsResult, JsSuccess, JsValue, Json, Reads, Writes}

import scala.util.Try

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

case class Paged[T](results: Seq[T], pageIndex: Int, pageSize: Int, resultCount: Long) {
  def map[X](f: T => X): Paged[X] = this.copy(results = results.map(f))
  def size: Int                   = results.size
  def pageCount: Int              = Math.ceil(resultCount.toDouble / pageSize).toInt
  def isEmpty: Boolean            = results.isEmpty
  def nonEmpty: Boolean           = results.nonEmpty
}

object Paged {
  def empty[T]: Paged[T]                         = Paged(Seq.empty, 1, 0, 0)
  def empty[T](pagination: Pagination): Paged[T] = Paged(Seq.empty, pagination, 0)
  def apply[T](results: Seq[T], pagination: Pagination, resultCount: Int): Paged[T] =
    Paged(results, pagination.pageIndex, pagination.pageSize, resultCount)
  def apply[T](results: Seq[T]): Paged[T]                   = Paged(results, SimplePagination(), results.size)
  def apply[T](results: Seq[T], resultCount: Int): Paged[T] = Paged(results, SimplePagination(), resultCount)

  implicit def format[T](implicit fmt: Format[T]): Format[Paged[T]] =
    Format[Paged[T]](Reads[Paged[T]](reads), Writes[Paged[T]](writes))

  private def reads[T](implicit fmt: Reads[T]): JsValue => JsResult[Paged[T]] =
    js =>
      Try(
        new Paged[T](
          js \ "results" match {
            case JsDefined(JsArray(r)) => r.map(jsResult => jsResult.as[T])
            case _                     => throw new IllegalArgumentException("invalid results")
          },
          (js \ "pageIndex").as[Int],
          (js \ "pageSize").as[Int],
          (js \ "resultCount").as[Int]
        )
      ).map(JsSuccess(_))
        .recover {
          case t: Throwable => JsError(t.getMessage)
        }
        .get

  private def writes[T](implicit fmt: Writes[T]): Paged[T] => JsValue =
    (paged: Paged[T]) =>
      Json.obj(
        "results"     -> JsArray(paged.results.map(fmt.writes)),
        "pageIndex"   -> JsNumber(paged.pageIndex),
        "pageSize"    -> JsNumber(paged.pageSize),
        "resultCount" -> JsNumber(paged.resultCount)
      )
}
