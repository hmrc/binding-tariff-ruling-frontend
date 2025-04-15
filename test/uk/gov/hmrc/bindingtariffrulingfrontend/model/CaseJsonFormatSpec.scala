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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.{Application, ApplicationType, Attachment, Case, CaseStatus, Decision}
import play.api.libs.json.{JsNumber, JsString}
import java.time.Instant

class CaseJsonFormatSpec extends BaseSpec {

  "Case.format" should {
    "convert Case to and from JSON for all status types" in {
      for (
        status <- List(
                    CaseStatus.DRAFT,
                    CaseStatus.NEW,
                    CaseStatus.OPEN,
                    CaseStatus.SUPPRESSED,
                    CaseStatus.REFERRED,
                    CaseStatus.REJECTED,
                    CaseStatus.CANCELLED,
                    CaseStatus.SUSPENDED,
                    CaseStatus.COMPLETED,
                    CaseStatus.REVOKED,
                    CaseStatus.ANNULLED
                  )
      ) {
        val testCase = Case(
          reference = s"test-reference-$status",
          status = status,
          application = Application(ApplicationType.BTI),
          decision = None,
          attachments = Seq.empty,
          keywords = Set.empty
        )

        val json   = Case.format.writes(testCase)
        val result = Case.format.reads(json).get

        result shouldBe testCase
      }
    }

    "handle Decision field correctly" in {
      val decision = Decision(
        bindingCommodityCode = "12345678",
        effectiveStartDate = Some(Instant.parse("2025-01-01T00:00:00Z")),
        effectiveEndDate = Some(Instant.parse("2028-01-01T00:00:00Z")),
        justification = "Test justification",
        goodsDescription = "Test description"
      )

      val testCase = Case(
        reference = "test-reference",
        status = CaseStatus.COMPLETED,
        application = Application(ApplicationType.BTI),
        decision = Some(decision),
        attachments = Seq.empty,
        keywords = Set.empty
      )

      val json   = Case.format.writes(testCase)
      val result = Case.format.reads(json).get

      result                                   shouldBe testCase
      result.decision.isDefined                shouldBe true
      result.decision.get.bindingCommodityCode shouldBe "12345678"
    }

    "handle Attachments correctly" in {
      val attachments = Seq(
        Attachment("id1", true, true),
        Attachment("id2", false, false)
      )

      val testCase = Case(
        reference = "test-reference",
        status = CaseStatus.OPEN,
        application = Application(ApplicationType.BTI),
        decision = None,
        attachments = attachments,
        keywords = Set.empty
      )

      val json   = Case.format.writes(testCase)
      val result = Case.format.reads(json).get

      result                         shouldBe testCase
      result.attachments.size        shouldBe 2
      result.attachments.head.id     shouldBe "id1"
      result.attachments.head.public shouldBe true
    }

    "handle Keywords correctly" in {
      val keywords = Set("keyword1", "keyword2", "keyword3")

      val testCase = Case(
        reference = "test-reference",
        status = CaseStatus.OPEN,
        application = Application(ApplicationType.BTI),
        decision = None,
        attachments = Seq.empty,
        keywords = keywords
      )

      val json   = Case.format.writes(testCase)
      val result = Case.format.reads(json).get

      result          shouldBe testCase
      result.keywords shouldBe keywords
    }
  }

  "CaseStatus format" should {
    "handle all valid status values" in {
      val allStatuses = List(
        "DRAFT",
        "NEW",
        "OPEN",
        "SUPPRESSED",
        "REFERRED",
        "REJECTED",
        "CANCELLED",
        "SUSPENDED",
        "COMPLETED",
        "REVOKED",
        "ANNULLED"
      )

      for (statusStr <- allStatuses) {
        val result = CaseStatus.given_Format_CaseStatus.reads(JsString(statusStr))
        result.isSuccess shouldBe true
      }
    }

    "reject invalid status values" in {
      val result = CaseStatus.given_Format_CaseStatus.reads(JsString("INVALID"))
      result.isError shouldBe true

      val nonStringResult = CaseStatus.given_Format_CaseStatus.reads(JsNumber(123))
      nonStringResult.isError shouldBe true
    }
  }
}
