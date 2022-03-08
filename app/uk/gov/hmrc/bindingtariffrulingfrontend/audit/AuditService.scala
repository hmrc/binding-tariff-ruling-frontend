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

package uk.gov.hmrc.bindingtariffrulingfrontend.audit

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class AuditService @Inject() (auditConnector: DefaultAuditConnector) {

  import AuditPayloadType._

  def auditRulingCreated(ruling: Ruling)(implicit hc: HeaderCarrier): Unit =
    sendExplicitAuditEvent(
      auditEventType = rulingCreated,
      auditPayload   = ruling
    )

  def auditRulingDeleted(reference: String)(implicit hc: HeaderCarrier): Unit =
    auditConnector.sendExplicitAudit(
      auditType = rulingDeleted,
      detail    = Map("reference" -> reference)
    )

  private def sendExplicitAuditEvent(auditEventType: String, auditPayload: Ruling)(implicit hc: HeaderCarrier): Unit =
    auditConnector
      .sendExplicitAudit(auditType = auditEventType, detail = auditPayload)(implicitly, implicitly, Ruling.REST.format)

}

object AuditPayloadType {
  val rulingCreated = "bindingTariffRulingCreated"
  val rulingDeleted = "bindingTariffRulingDeleted"
}
