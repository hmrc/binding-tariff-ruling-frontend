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

package uk.gov.hmrc.bindingtariffrulingfrontend.repository

import com.google.inject.ImplementedBy
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonArray, BsonDocument}
import org.mongodb.scala.gridfs.{ObservableFuture, SingleObservableFuture}
import org.mongodb.scala.model.*
import org.mongodb.scala.model.Filters.*
import org.mongodb.scala.model.Indexes.{ascending, compoundIndex, descending}
import org.mongodb.scala.{ObservableFuture, ReadConcern, SingleObservableFuture}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.{LocalDate, ZoneId}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[RulingMongoRepository])
trait RulingRepository {

  def update(ruling: Ruling, upsert: Boolean): Future[Boolean]

  def get(id: String): Future[Option[Ruling]]

  def get(search: SimpleSearch): Future[Paged[Ruling]]

  def delete(id: String): Future[Unit]

  def deleteAll(): Future[Unit]

}

@Singleton
class RulingMongoRepository @Inject() (mongoComponent: MongoComponent)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[Ruling](
      collectionName = "rulings",
      mongoComponent = mongoComponent,
      domainFormat = Ruling.Mongo.format,
      indexes = Seq(
        IndexModel(ascending("reference"), IndexOptions().unique(true).background(false).name("reference_Index")),
        IndexModel(
          ascending("bindingCommodityCode"),
          IndexOptions().unique(false).background(false).name("bindingCommodityCode_Index")
        ),
        IndexModel(
          compoundIndex(
            Indexes.text("reference"),
            Indexes.text("bindingCommodityCode"),
            Indexes.text("bindingCommodityCodeNGrams"),
            Indexes.text("justification"),
            Indexes.text("goodsDescription"),
            Indexes.text("keywords")
          ),
          IndexOptions()
            .unique(false)
            .name("textIndex")
            .background(false)
            .weights(
              BsonDocument("bindingCommodityCode" -> 10, "bindingCommodityCodeNgrams" -> 10, "keywords" -> 5)
            )
        )
      )
    )
    with RulingRepository {

  // ATAR is holding data for long period of time, now it is set to 3 years but other ATAR services don't have the
  // the index so we'll skip this one as well, as this index should be implemented the same way in all other ATAR services
  override lazy val requiresTtlIndex: Boolean = false

  override def update(ruling: Ruling, upsert: Boolean): Future[Boolean] =
    collection
      .findOneAndReplace(
        filter = byReference(ruling.reference),
        replacement = ruling,
        options = FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
      )
      .toFuture()
      .map(_ => true)

  override def get(reference: String): Future[Option[Ruling]] =
    collection.find(byReference(reference)).first().toFutureOption()

  override def delete(reference: String): Future[Unit] =
    collection.findOneAndDelete(byReference(reference)).toFuture().map(_ => ())

  override def deleteAll(): Future[Unit] =
    collection.deleteMany(BsonDocument()).toFuture().map(_ => ())

  override def get(search: SimpleSearch): Future[Paged[Ruling]] = {

    val startOfToday = LocalDate.now().atStartOfDay
    val zoneOffset   = ZoneId.of("Europe/London").getRules.getOffset(startOfToday)
    val today        = startOfToday.toInstant(zoneOffset)

    val textSearch  = search.query.map(query => Filters.text(query)).toSeq
    val imageFilter = if (search.imagesOnly) Seq(Filters.gt("images", BsonArray())) else Seq.empty
    val dateFilter  = Seq(Filters.gt("effectiveEndDate", today))

    val allSearches: Seq[Bson] = textSearch ++ imageFilter ++ dateFilter
    val findSearches           = if (allSearches.nonEmpty) and(allSearches: _*) else BsonDocument()

    val textScore = if (search.query.isDefined) Sorts.metaTextScore("score") else BsonDocument()

    val withOrWithoutProjectionSearch =
      if (textSearch.isEmpty) {
        collection
          .find(findSearches)
      } else {
        collection
          .find(findSearches)
          .projection(Projections.metaTextScore("score"))
      }

    for {
      results <- withOrWithoutProjectionSearch
                   .skip((search.pageIndex - 1) * search.pageSize)
                   .limit(search.pageSize)
                   .sort(Sorts.orderBy(textScore, descending("effectiveEndDate"), descending("reference")))
                   .toFuture()
      count <- collection
                 .withReadConcern(ReadConcern.MAJORITY)
                 .countDocuments(and(allSearches: _*), CountOptions().skip(0))
                 .toFuture()
    } yield Paged(results, search.pageIndex, search.pageSize, count)
  }

  private def byReference(reference: String) =
    equal("reference", reference)

}
