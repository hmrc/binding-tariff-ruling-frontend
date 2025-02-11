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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import com.codahale.metrics.MetricRegistry
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.Source
import org.apache.pekko.util.ByteString
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.metrics.HasMetrics
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.readStreamHttpResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileStoreConnector @Inject() (
  appConfig: AppConfig,
  httpClient: HttpClientV2,
  val metrics: MetricRegistry
)(implicit val mat: Materializer)
    extends HasMetrics
    with InjectAuthHeader {

  implicit val ec: ExecutionContext =
    mat.executionContext

  type Metadata = Map[String, FileMetadata]
  private lazy val noMetadata: Metadata = Map.empty

  private val ParamLength =
    42 // A 36-char UUID plus &id= and some wiggle room
  private val BatchSize =
    ((appConfig.maxUriLength - appConfig.bindingTariffFileStoreUrl.length) / ParamLength).intValue()

  private def makeQuery(ids: Seq[String]): String = {
    val query = s"?${ids.map("id=" + _).mkString("&")}"
    s"${appConfig.bindingTariffFileStoreUrl}/file$query"
  }

  def get(attachmentId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[FileMetadata]] =
    withMetricsTimerAsync("get-attachment-metadata") { _ =>
      val fullURL = s"${appConfig.bindingTariffFileStoreUrl}/file/$attachmentId"

      httpClient
        .get(url"$fullURL")
        .setHeader(authHeaders(appConfig): _*)
        .execute[Option[FileMetadata]]
        .map(_.filter(_.published))
    }

  def get(attachmentIds: Set[String])(implicit headerCarrier: HeaderCarrier): Future[Metadata] =
    withMetricsTimerAsync("get-attachment-metadata-multiple") { _ =>
      if (attachmentIds.isEmpty) {
        Future.successful(Map.empty)
      } else {
        Source(attachmentIds)
          .grouped(BatchSize)
          .mapAsyncUnordered(Runtime.getRuntime.availableProcessors()) { ids =>
            httpClient
              .get(url"${makeQuery(ids)}")
              .setHeader(authHeaders(appConfig): _*)
              .execute[Seq[FileMetadata]]
          }
          .runFold(noMetadata) { case (metadata, newEntries) =>
            metadata ++ newEntries.filter(_.published).map(entry => entry.id -> entry).toMap
          }
      }
    }

  def downloadFile(fileURL: String)(implicit hc: HeaderCarrier): Future[Option[Source[ByteString, _]]] =
    withMetricsTimerAsync("download-file") { _ =>
      httpClient
        .get(url"$fileURL")
        .setHeader(authHeaders(appConfig): _*)
        .stream[HttpResponse]
        .flatMap { response =>
          if (response.status / 100 == 2) {
            Future.successful(Some(response.bodyAsSource))
          } else if (response.status / 100 > 4) {
            Future.failed(new RuntimeException("Unable to retrieve file from filestore"))
          } else {
            Future.successful(None)
          }
        }
    }
}
