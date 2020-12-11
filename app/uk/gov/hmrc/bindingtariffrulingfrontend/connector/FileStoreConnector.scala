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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import akka.stream.scaladsl.Source
import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileStoreConnector @Inject() (
  appConfig: AppConfig,
  http: AuthenticatedHttpClient
)(implicit mat: Materializer) {

  implicit val ec: ExecutionContext =
    mat.executionContext

  type Metadata = Map[String, FileMetadata]
  private lazy val noMetadata: Metadata = Map.empty

  private val ParamLength = 40 // A 36-char UUID plus &id=
  private val BatchSize   = (appConfig.maxUriLength / ParamLength).intValue()

  private def makeQuery(ids: Seq[String]): String = {
    val query = s"?${ids.map("id=" + _).mkString("&")}"
    s"${appConfig.bindingTariffFileStoreUrl}/file$query"
  }

  def get(attachmentId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[FileMetadata]] =
    http
      .GET[Option[FileMetadata]](s"${appConfig.bindingTariffFileStoreUrl}/file/$attachmentId")
      .map(_.filter(_.published))

  def get(attachmentIds: Set[String])(implicit headerCarrier: HeaderCarrier): Future[Metadata] =
    if (attachmentIds.isEmpty)
      Future.successful(Map.empty)
    else
      Source(attachmentIds)
        .grouped(BatchSize)
        .mapAsyncUnordered(Runtime.getRuntime().availableProcessors()) { ids =>
          http.GET[Seq[FileMetadata]](makeQuery(ids))
        }
        .runFold(noMetadata) {
          case (metadata, newEntries) =>
            metadata ++ newEntries.filter(_.published).map(entry => entry.id -> entry).toMap
        }
}
