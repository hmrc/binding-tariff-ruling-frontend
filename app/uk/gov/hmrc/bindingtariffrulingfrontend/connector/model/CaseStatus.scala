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

import play.api.libs.json.*

enum CaseStatus:
  case DRAFT, NEW, OPEN, SUPPRESSED, REFERRED, REJECTED, CANCELLED, SUSPENDED, COMPLETED, REVOKED, ANNULLED

object CaseStatus:
  given Format[CaseStatus] = Format(
    Reads {
      case JsString("DRAFT")      => JsSuccess(CaseStatus.DRAFT)
      case JsString("NEW")        => JsSuccess(CaseStatus.NEW)
      case JsString("OPEN")       => JsSuccess(CaseStatus.OPEN)
      case JsString("SUPPRESSED") => JsSuccess(CaseStatus.SUPPRESSED)
      case JsString("REFERRED")   => JsSuccess(CaseStatus.REFERRED)
      case JsString("REJECTED")   => JsSuccess(CaseStatus.REJECTED)
      case JsString("CANCELLED")  => JsSuccess(CaseStatus.CANCELLED)
      case JsString("SUSPENDED")  => JsSuccess(CaseStatus.SUSPENDED)
      case JsString("COMPLETED")  => JsSuccess(CaseStatus.COMPLETED)
      case JsString("REVOKED")    => JsSuccess(CaseStatus.REVOKED)
      case JsString("ANNULLED")   => JsSuccess(CaseStatus.ANNULLED)
      case JsString(other)        => JsError(s"Unknown CaseStatus: $other")
      case _                      => JsError("String value expected")
    },
    Writes {
      case CaseStatus.DRAFT      => JsString("DRAFT")
      case CaseStatus.NEW        => JsString("NEW")
      case CaseStatus.OPEN       => JsString("OPEN")
      case CaseStatus.SUPPRESSED => JsString("SUPPRESSED")
      case CaseStatus.REFERRED   => JsString("REFERRED")
      case CaseStatus.REJECTED   => JsString("REJECTED")
      case CaseStatus.CANCELLED  => JsString("CANCELLED")
      case CaseStatus.SUSPENDED  => JsString("SUSPENDED")
      case CaseStatus.COMPLETED  => JsString("COMPLETED")
      case CaseStatus.REVOKED    => JsString("REVOKED")
      case CaseStatus.ANNULLED   => JsString("ANNULLED")

    }
  )
