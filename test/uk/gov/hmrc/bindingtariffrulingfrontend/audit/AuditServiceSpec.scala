/*
 * Copyright 2024 HM Revenue & Customs
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

import org.mockito.Mockito.{mock, verify}

import java.time.Instant
import uk.gov.hmrc.bindingtariffrulingfrontend.audit.AuditPayloadType._
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.play.audit.DefaultAuditConnector

import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends BaseSpec {

  private val auditConnector: DefaultAuditConnector = mock(classOf[DefaultAuditConnector])

  private val service: AuditService = new AuditService(auditConnector)

  "AuditService" should {

    val ruling = Ruling(
      reference = "reference",
      bindingCommodityCode = "bindingCommodityCode",
      effectiveStartDate = Instant.now,
      effectiveEndDate = Instant.now,
      justification = "justification",
      goodsDescription = "goodsDescription",
      keywords = Set("k1", "k2", "k3"),
      attachments = Seq("f1", "f2", "f3")
    )

    "call the audit connector when a ruling is created" in {
      service.auditRulingCreated(ruling)

      verify(auditConnector).sendExplicitAudit(rulingCreated, ruling)(hc, global, Ruling.REST.format)
    }

    "call the audit connector when a ruling is deleted" in {
      service.auditRulingDeleted(ruling.reference)

      verify(auditConnector).sendExplicitAudit(rulingDeleted, Map("reference" -> ruling.reference))(hc, global)
    }
  }

}
