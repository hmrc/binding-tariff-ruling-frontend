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

import com.kenshoo.play.metrics.Metrics
import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FileStoreConnector @Inject() (
  appConfig: AppConfig,
  http: AuthenticatedHttpClient,
  ws: WSClient,
  val metrics: Metrics
)(implicit ec: ExecutionContext) {

  def get(attachmentIds: Seq[String])(implicit headerCarrier: HeaderCarrier): Future[Map[String, FileMetadata]] =
    if (attachmentIds.isEmpty) {
      Future.successful(Map.empty)
    } else {
      val query = s"?${attachmentIds.map(att => s"id=$att").mkString("&")}"
      http
        .GET[Seq[FileMetadata]](s"${appConfig.bindingTariffFileStoreUrl}/file$query")
        .map(_.map(meta => (meta.id, meta)).toMap)
    }
}
