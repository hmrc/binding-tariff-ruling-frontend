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

package uk.gov.hmrc.bindingtariffrulingfrontend.repository

import com.google.inject.ImplementedBy
import javax.inject.{Inject, Singleton}
import play.api.libs.json._
import reactivemongo.api.indexes.Index
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.MongoIndexCreator.createSingleFieldAscendingIndex
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[RulingMongoRepository])
trait RulingRepository {

  def update(ruling: Ruling, upsert: Boolean): Future[Ruling]

  def get(id: String): Future[Option[Ruling]]

  def get(search: SimpleSearch): Future[Paged[Ruling]]

  def delete(id: String): Future[Unit]

  def delete(): Future[Unit]

}

@Singleton
class RulingMongoRepository @Inject()(config: AppConfig,
                                      mongoDbProvider: MongoDbProvider)
  extends ReactiveRepository[Ruling, BSONObjectID](
    collectionName = "rulings",
    mongo = mongoDbProvider.mongo,
    domainFormat = Ruling.format) with RulingRepository {

  override lazy val indexes: Seq[Index] = Seq(
    createSingleFieldAscendingIndex("reference", isUnique = true)
  )

  override def ensureIndexes(implicit ec: ExecutionContext): Future[Seq[Boolean]] = {
    Future.sequence(indexes.map(collection.indexesManager.ensure(_)))
  }

  override def update(ruling: Ruling, upsert: Boolean): Future[Ruling] = collection.findAndUpdate(
    selector = byReference(ruling.reference),
    update = ruling,
    fetchNewObject = true,
    upsert = upsert
  ).map(_.value.map(_.as[Ruling]).get)

  override def get(reference: String): Future[Option[Ruling]] = collection.find(byReference(reference)).one[Ruling]

  override def delete(reference: String): Future[Unit] = collection.findAndRemove(byReference(reference)).map(_ => ())

  override def delete(): Future[Unit] = {
    removeAll().map(_ => ())
  }

  override def get(search: SimpleSearch): Future[Paged[Ruling]] = {
    val filter = either(
      "reference" -> eq(search.query),
      "bindingCommodityCode" -> numberStartingWith(search.query),
      "goodsDescription" -> contains(search.query)
    )
    for {
      results <- collection.find[JsObject, Ruling](filter)
        .options(QueryOpts(skipN = (search.pageIndex - 1) * search.pageSize, batchSizeN = search.pageSize))
        .cursor[Ruling]()
        .collect[List](search.pageSize, Cursor.FailOnError[List[Ruling]]())
      count <- collection.count(Some(filter))
    } yield Paged(results, search.pageIndex, search.pageSize, count)
  }

  private def byReference(reference: String): JsObject = {
    Json.obj("reference" -> reference)
  }

  private def eq(string: String): JsValue = JsString(string)

  private def numberStartingWith(value: String): JsValue = regex(s"^$value\\d*")

  private def contains(value: String): JsValue = regex(s".*$value.*", ignoreCase = true)

  private def regex(regex: String, ignoreCase: Boolean = false): JsValue = Json.obj(
    "$regex" -> regex,
    "$options" -> (if(ignoreCase) "i" else "")
  )

  private def either(options: (String, JsValue)*): JsObject = {
    Json.obj("$or" -> JsArray(
      options.map(element => Json.obj(element._1 -> element._2)))
    )
  }
}
