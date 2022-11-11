/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.metrics.HasMetrics
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileStoreConnector @Inject() (
  appConfig: AppConfig,
  http: AuthenticatedHttpClient,
  ws: WSClient,
  val metrics: Metrics
)(implicit mat: Materializer)
    extends InjectAuthHeader
    with HasMetrics {

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
      http
        .GET[Option[FileMetadata]](
          s"${appConfig.bindingTariffFileStoreUrl}/file/$attachmentId",
          headers = authHeaders(appConfig)
        )
        .map(_.filter(_.published))
    }

  def get(attachmentIds: Set[String])(implicit headerCarrier: HeaderCarrier): Future[Metadata] =
    withMetricsTimerAsync("get-attachment-metadata-multiple") { _ =>
      if (attachmentIds.isEmpty) {
        Future.successful(Map.empty)
      } else {
        Source(attachmentIds)
          .grouped(BatchSize)
          .mapAsyncUnordered(Runtime.getRuntime().availableProcessors()) { ids =>
            http.GET[Seq[FileMetadata]](makeQuery(ids), headers = authHeaders(appConfig))
          }
          .runFold(noMetadata) {
            case (metadata, newEntries) =>
              metadata ++ newEntries.filter(_.published).map(entry => entry.id -> entry).toMap
          }
      }
    }

  def downloadFile(url: String)(implicit hc: HeaderCarrier): Future[Option[Source[ByteString, _]]] =
    withMetricsTimerAsync("download-file") { _ =>
      lazy val hcConfig = HeaderCarrier.Config.fromConfig(http.configuration)

      val fileStoreResponse = ws
        .url(url)
        .withHttpHeaders(hc.withExtraHeaders(http.authHeaders(appConfig): _*).headersForUrl(hcConfig)(url): _*)
        .get()

      fileStoreResponse.flatMap { response =>
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
