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

package uk.gov.hmrc.bindingtariffrulingfrontend.service

import akka.stream.scaladsl.Source
import akka.util.ByteString
import javax.inject.{Inject, Singleton}
import play.api.Logging
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.FileStoreConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

@Singleton
class FileStoreService @Inject() (connector: FileStoreConnector) extends Logging {
  type Metadata = Map[String, FileMetadata]

  def get(attachmentId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[FileMetadata]] =
    connector.get(attachmentId)

  def get(attachmentIds: Seq[String])(implicit headerCarrier: HeaderCarrier): Future[Metadata] =
    connector.get(attachmentIds.toSet)

  def get(ruling: Ruling)(implicit headerCarrier: HeaderCarrier): Future[Metadata] =
    get(ruling.attachments ++ ruling.images)

  def get(paged: Paged[Ruling])(implicit headerCarrier: HeaderCarrier): Future[Metadata] = {
    val attachmentIds = paged.results.flatMap(result => result.attachments ++ result.images)
    get(attachmentIds)
  }

  def downloadFile(url: String)(implicit hc: HeaderCarrier): Future[Option[Source[ByteString, _]]] =
    connector.downloadFile(url)
}
