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

package uk.gov.hmrc.bindingtariffrulingfrontend.service

import javax.inject.Inject
import uk.gov.hmrc.bindingtariffrulingfrontend.audit.AuditService
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.{Case, CaseStatus, Decision}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.RulingRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RulingService @Inject()(repository: RulingRepository,
                              auditService: AuditService,
                              bindingTariffClassificationConnector: BindingTariffClassificationConnector) {

  def delete(): Future[Unit] = {
    repository.delete()
  }

  def get(reference: String): Future[Option[Ruling]] = {
    repository.get(reference)
  }

  def get(query: SimpleSearch): Future[Paged[Ruling]] = {
    repository.get(query)
  }

  def refresh(reference: String)(implicit hc: HeaderCarrier): Future[Unit] = {

    type ExistingRuling = Option[Ruling]
    type UpdatedRuling = Option[Ruling]
    type RulingUpdate = (ExistingRuling, UpdatedRuling)

    val rulingUpdate: Future[RulingUpdate] = for {
      existingRuling: ExistingRuling <- repository.get(reference)
      updatedCase: Option[Case] <- bindingTariffClassificationConnector.get(reference)
      updatedRuling: UpdatedRuling = updatedCase
        .filter(_.status == CaseStatus.COMPLETED)
        .filter(_.decision.isDefined)
        .filter(_.decision.flatMap(_.effectiveStartDate).isDefined)
        .filter(_.decision.flatMap(_.effectiveEndDate).isDefined)
        .map(toRuling)
      result: RulingUpdate = (existingRuling, updatedRuling)
    } yield result

    rulingUpdate flatMap {

      case (Some(_), Some(u)) =>
        for {
          _ <- repository.update(u, upsert = false)
          _ = auditService.auditRulingUpdated(u)
        } yield ()

      case (Some(_), None) =>
        for {
          _ <- repository.delete(reference)
          _ = auditService.auditRulingDeleted(reference)
        } yield ()

      case (None, Some(u)) =>
        for {
          _ <- repository.update(u, upsert = true)
          _ = auditService.auditRulingCreated(u)
        } yield ()

      case _ => Future.successful(())

    }

  }

  private def toRuling(c: Case): Ruling = {
    val keywords: Set[String] = c.keywords
    val attachments: Seq[String] = c.attachments.filter(_.public).map(_.id)
    val reference: String = c.reference
    val decision: Decision = c.decision.get

    Ruling(
      reference = reference,
      bindingCommodityCode = decision.bindingCommodityCode,
      effectiveStartDate = decision.effectiveStartDate.get,
      effectiveEndDate = decision.effectiveEndDate.get,
      justification = decision.justification,
      goodsDescription = decision.goodsDescription,
      keywords = keywords,
      attachments = attachments
    )
  }

}
