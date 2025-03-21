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

import play.api.libs.json.Json
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.{Application, ApplicationType, Attachment, Case, CaseStatus, Decision}

class CaseJsonFormatSpec extends BaseSpec {
  "Case.format" should {
    "convert Case to and from JSON" in {
      // Create a sample Case
      val sampleCase = Case(
        reference = "test-reference",
        status = CaseStatus.COMPLETED,
        application = Application(ApplicationType.BTI),
        decision = None,
        attachments = Seq.empty,
        keywords = Set.empty
      )

      // Use the formatter directly
      val formatter = Case.format

      // Convert to JSON
      val json = formatter.writes(sampleCase)

      // Verify JSON has expected fields
      (json \ "reference").as[String] shouldBe "test-reference"
      (json \ "status").as[String]    shouldBe "COMPLETED"

      // Convert back to Case
      val caseFromJson = formatter.reads(json).get

      // Verify round-trip works
      caseFromJson shouldBe sampleCase
    }
  }
}
