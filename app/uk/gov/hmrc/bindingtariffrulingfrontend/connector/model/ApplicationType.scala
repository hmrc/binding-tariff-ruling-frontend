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

enum ApplicationType {
  case BTI, LIABILITY_ORDER, CORRESPONDENCE, MISCELLANEOUS
}

object ApplicationType {
  implicit val format: Format[ApplicationType] = new Format[ApplicationType] {

    def writes(applicationType: ApplicationType): JsValue = applicationType match
      case BTI             => JsString("BTI")
      case LIABILITY_ORDER => JsString("LIABILITY_ORDER")
      case CORRESPONDENCE  => JsString("CORRESPONDENCE")
      case MISCELLANEOUS   => JsString("MISCELLANEOUS")

    def reads(json: JsValue): JsResult[ApplicationType] = json match {
      case JsString(s) =>
        try
          s match {
            case "BTI"             => JsSuccess(ApplicationType.BTI)
            case "LIABILITY_ORDER" => JsSuccess(ApplicationType.LIABILITY_ORDER)
            case "CORRESPONDENCE"  => JsSuccess(ApplicationType.CORRESPONDENCE)
            case "MISCELLANEOUS"   => JsSuccess(ApplicationType.MISCELLANEOUS)
            case _                 => JsError(s"Unknown value for CaseStatus")
          }
        catch {
          case _: NoSuchElementException =>
            JsError(s"Unknown value for CaseStatus")
        }

      case _ => JsError("String value expected")
    }
  }
}
