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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector.model

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Writes}

enum CaseStatus {
  case DRAFT, NEW, OPEN, SUPPRESSED, REFERRED, REJECTED, CANCELLED, SUSPENDED, COMPLETED, REVOKED, ANNULLED
}

object CaseStatus {
  implicit val format: Format[CaseStatus] = new Format[CaseStatus] {

    /*def writes: Writes[CaseStatus] = Writes {
      case DRAFT      => JsString("DRAFT")
      case NEW        => JsString("NEW")
      case OPEN       => JsString("OPEN")
      case SUPPRESSED => JsString("SUPPRESSED")
      case REFERRED   => JsString("REFERRED")
      case REJECTED   => JsString("REJECTED")
      case CANCELLED  => JsString("CANCELLED")
      case SUSPENDED  => JsString("SUSPENDED")
      case COMPLETED  => JsString("COMPLETED")
      case REVOKED    => JsString("REVOKED")
      case ANNULLED   => JsString("ANNULLED")
    }
     */
    def writes(caseStatus: CaseStatus): JsValue = caseStatus match
      case DRAFT      => JsString("DRAFT")
      case NEW        => JsString("NEW")
      case OPEN       => JsString("OPEN")
      case SUPPRESSED => JsString("SUPPRESSED")
      case REFERRED   => JsString("REFERRED")
      case REJECTED   => JsString("REJECTED")
      case CANCELLED  => JsString("CANCELLED")
      case SUSPENDED  => JsString("SUSPENDED")
      case COMPLETED  => JsString("COMPLETED")
      case REVOKED    => JsString("REVOKED")
      case ANNULLED   => JsString("ANNULLED")

    def reads(json: JsValue): JsResult[CaseStatus] = json match {
      case JsString(s) =>
        try
          s match {
            case "DRAFT"      => JsSuccess(CaseStatus.DRAFT)
            case "NEW"        => JsSuccess(CaseStatus.NEW)
            case "OPEN"       => JsSuccess(CaseStatus.OPEN)
            case "SUPPRESSED" => JsSuccess(CaseStatus.SUPPRESSED)
            case "REFERRED"   => JsSuccess(CaseStatus.REFERRED)
            case "REJECTED"   => JsSuccess(CaseStatus.REJECTED)
            case "CANCELLED"  => JsSuccess(CaseStatus.CANCELLED)
            case "SUSPENDED"  => JsSuccess(CaseStatus.SUSPENDED)
            case "COMPLETED"  => JsSuccess(CaseStatus.COMPLETED)
            case "REVOKED"    => JsSuccess(CaseStatus.REVOKED)
            case "ANNULLED"   => JsSuccess(CaseStatus.ANNULLED)
            case _            => JsError(s"Unknown value for CaseStatus")
          }
        catch {
          case _: NoSuchElementException =>
            JsError(s"Unknown value for CaseStatus")
        }

      case _ => JsError("String value expected")
    }
  }
}
