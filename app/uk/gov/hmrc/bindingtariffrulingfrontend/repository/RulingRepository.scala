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

package uk.gov.hmrc.bindingtariffrulingfrontend.repository

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.api.{Cursor, QueryOpts, ReadConcern}
import reactivemongo.bson.{BSONDocument, BSONInteger, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.MongoIndexCreator._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.LocalDate
import java.time.ZoneOffset

@ImplementedBy(classOf[RulingMongoRepository])
trait RulingRepository {

  def update(ruling: Ruling, upsert: Boolean): Future[Ruling]

  def get(id: String): Future[Option[Ruling]]

  def get(search: SimpleSearch): Future[Paged[Ruling]]

  def delete(id: String): Future[Unit]

  def deleteAll(): Future[Unit]

}

@Singleton
class RulingMongoRepository @Inject() (mongoDbProvider: MongoDbProvider)(implicit val ec: ExecutionContext)
    extends ReactiveRepository[Ruling, BSONObjectID](
      collectionName = "rulings",
      mongo          = mongoDbProvider.mongo,
      domainFormat   = Ruling.Mongo.format
    )
    with RulingRepository {
  import Ruling.Mongo.format

  override lazy val indexes: Seq[Index] = Seq(
    createSingleFieldAscendingIndex("reference", isUnique = true),
    createSingleFieldAscendingIndex("bindingCommodityCode"),
    createCompoundIndex(
      name = Some("textIndex"),
      indexFieldMappings = Seq(
        "reference"                  -> IndexType.Text,
        "bindingCommodityCode"       -> IndexType.Text,
        "bindingCommodityCodeNGrams" -> IndexType.Text,
        "justification"              -> IndexType.Text,
        "goodsDescription"           -> IndexType.Text,
        "keywords"                   -> IndexType.Text
      ),
      options = BSONDocument(
        "weights" -> BSONDocument(
          "bindingCommodityCode"       -> BSONInteger(10),
          "bindingCommodityCodeNgrams" -> BSONInteger(10),
          "keywords"                   -> BSONInteger(5)
        )
      )
    )
  )

  override def update(ruling: Ruling, upsert: Boolean): Future[Ruling] =
    collection
      .findAndUpdate(
        selector       = byReference(ruling.reference),
        update         = ruling,
        fetchNewObject = true,
        upsert         = upsert
      )
      .map(_.value.map(_.as[Ruling]).get)

  override def get(reference: String): Future[Option[Ruling]] = collection.find(byReference(reference)).one[Ruling]

  override def delete(reference: String): Future[Unit] = collection.findAndRemove(byReference(reference)).map(_ => ())

  override def deleteAll(): Future[Unit] =
    removeAll().map(_ => ())

  override def get(search: SimpleSearch): Future[Paged[Ruling]] = {
    val startOfToday = LocalDate.now().atStartOfDay
    val zoneOffset = ZoneId.of("Europe/London").getRules().getOffset(startOfToday)
    val today = Json.toJson(startOfToday.toInstant(zoneOffset))(Ruling.Mongo.formatInstant)

    val dateFilter = gt("effectiveEndDate", today)
    val textSearch = search.query.map(text(_)).getOrElse(Json.obj())
    val imageFilter =  if (search.imagesOnly) nonEmpty("attachments") else Json.obj()

    val allSearches: JsObject = dateFilter ++ textSearch ++ imageFilter

    val textScore: JsObject =
      Json.obj("score" -> Json.obj("$meta" -> "textScore"))

    for {
      results <- collection
                  .find[JsObject, JsObject](
                    selector   = allSearches,
                    projection = Some(textScore)
                  )
                  .options(QueryOpts(skipN = (search.pageIndex - 1) * search.pageSize, batchSizeN = search.pageSize))
                  .sort(textScore)
                  .cursor[JsObject]()
                  .collect[List](search.pageSize, Cursor.FailOnError[List[JsObject]]())
                  .map(_.map(_.as[Ruling](Ruling.Mongo.format.reads)))

      count <- collection.count(
                selector    = Some(allSearches),
                limit       = None,
                skip        = 0,
                hint        = None,
                readConcern = ReadConcern.Available
              )

    } yield Paged(results, search.pageIndex, search.pageSize, count)
  }

  private def byReference(reference: String): JsObject =
    Json.obj("reference" -> reference)

  private def text(query: String): JsObject =
    Json.obj("$text" -> Json.obj("$search" -> query))

  private def nonEmpty(field: String): JsObject =
    gt(field, Json.arr())

  private def gt(field: String, value: JsValue): JsObject =
    Json.obj(field -> Json.obj("$gt" -> value))
}
