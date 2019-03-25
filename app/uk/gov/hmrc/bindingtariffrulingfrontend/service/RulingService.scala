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
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.{Case, CaseStatus, Decision}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.RulingRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RulingService @Inject()(repository: RulingRepository, bindingTariffClassificationConnector: BindingTariffClassificationConnector) {

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
    val rulingPair: Future[(Option[Ruling], Option[Ruling])] = for {
      existingRuling <- repository.get(reference)
      updatedCase: Option[Case] <- bindingTariffClassificationConnector.get(reference)
      updatedRuling: Option[Ruling] = updatedCase
        .filter(_.status == CaseStatus.COMPLETED)
        .filter(_.decision.isDefined)
        .filter(_.decision.flatMap(_.effectiveStartDate).isDefined)
        .filter(_.decision.flatMap(_.effectiveEndDate).isDefined)
        .map(toRuling)
    } yield (existingRuling, updatedRuling)

    rulingPair flatMap {
      case (Some(_), Some(u)) => repository.update(u, upsert = false).map(_ => ())
      case (Some(_), None) => repository.delete(reference)
      case (None, Some(u)) => repository.update(u, upsert = true).map(_ => ())
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
