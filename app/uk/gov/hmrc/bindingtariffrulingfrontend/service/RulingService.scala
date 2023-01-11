/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.Logging
import uk.gov.hmrc.bindingtariffrulingfrontend.audit.AuditService
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model._
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.RulingRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RulingService @Inject() (
  repository: RulingRepository,
  auditService: AuditService,
  fileStoreService: FileStoreService,
  bindingTariffClassificationConnector: BindingTariffClassificationConnector
) extends Logging {

  type ExistingRuling = Option[Ruling]
  type UpdatedRuling  = Option[Ruling]
  type RulingUpdate   = (ExistingRuling, UpdatedRuling)

  def delete(reference: String): Future[Unit] =
    repository.delete(reference)

  def deleteAll(): Future[Unit] =
    repository.deleteAll()

  def get(reference: String): Future[Option[Ruling]] =
    repository.get(reference)

  def get(query: SimpleSearch): Future[Paged[Ruling]] =
    repository.get(query)

  def refresh(reference: String)(implicit hc: HeaderCarrier): Future[Unit] =
    bindingTariffClassificationConnector.get(reference).flatMap(refresh(reference, _))

  def refresh(reference: String, updatedCase: Option[Case])(implicit hc: HeaderCarrier): Future[Unit] = {

    val rulingUpdate: Future[RulingUpdate] =
      for {
        existingRuling: ExistingRuling <- repository.get(reference)
        validCase = updatedCase
          .filter(_.application.`type` == ApplicationType.BTI)
          .filter(_.status == CaseStatus.COMPLETED)
          .filter(_.decision.isDefined)
          .filter(_.decision.flatMap(_.effectiveStartDate).isDefined)
          .filter(_.decision.flatMap(_.effectiveEndDate).isDefined)
        fileMetaData <- validCase
                         .map(_.attachments.map(_.id))
                         .map(fileStoreService.get(_))
                         .getOrElse(Future.successful(Map.empty[String, FileMetadata]))
        updatedRuling: UpdatedRuling = validCase.map(toRuling(_, fileMetaData))
        result: RulingUpdate         = (existingRuling, updatedRuling)
      } yield result

    rulingUpdate.flatMap {
      case (Some(_), None) =>
        for {
          _ <- repository.delete(reference)
          _ = auditService.auditRulingDeleted(reference)
          _ = logger.info(s"Ruling has been deleted for case with reference: $reference")
        } yield ()
      case (None, Some(u)) =>
        for {
          _ <- repository.update(u, upsert = true)
          _ = auditService.auditRulingCreated(u)
          _ = logger.info(s"Ruling has been created for case with reference: ${u.reference}")
        } yield ()

      case (Some(_), Some(u)) =>
        repository.update(u, upsert = false).map { _ =>
          logger.info(s"Ruling has been updated for case with reference: ${u.reference}")
        }

      case _ => Future.successful(())

    }

  }

  private def toRuling(cse: Case, fileMetaData: Map[String, FileMetadata]): Ruling = {
    val keywords: Set[String] = cse.keywords
    val reference: String     = cse.reference
    val decision: Decision    = cse.decision.get

    val (images, attachments) = cse.attachments
      .filter(att => att.public && att.shouldPublishToRulings)
      .flatMap(att => fileMetaData.get(att.id))
      .partition(_.isImage)

    Ruling(
      reference            = reference,
      bindingCommodityCode = decision.bindingCommodityCode,
      effectiveStartDate   = decision.effectiveStartDate.get,
      effectiveEndDate     = decision.effectiveEndDate.get,
      justification        = decision.justification,
      goodsDescription     = decision.goodsDescription,
      keywords             = keywords,
      attachments          = attachments.map(_.id),
      images               = images.map(_.id)
    )
  }

}
