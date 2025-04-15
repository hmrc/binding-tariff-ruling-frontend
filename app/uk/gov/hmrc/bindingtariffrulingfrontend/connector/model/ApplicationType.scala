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

enum ApplicationType:
  case BTI, LIABILITY_ORDER, CORRESPONDENCE, MISCELLANEOUS

object ApplicationType:
  given Format[ApplicationType] = Format(
    Reads {
      case JsString("BTI")             => JsSuccess(ApplicationType.BTI)
      case JsString("LIABILITY_ORDER") => JsSuccess(ApplicationType.LIABILITY_ORDER)
      case JsString("CORRESPONDENCE")  => JsSuccess(ApplicationType.CORRESPONDENCE)
      case JsString("MISCELLANEOUS")   => JsSuccess(ApplicationType.MISCELLANEOUS)
      case JsString(other)             => JsError(s"Unknown ApplicationType: $other")
      case _                           => JsError("String value expected")
    },
    Writes {
      case ApplicationType.BTI             => JsString("BTI")
      case ApplicationType.LIABILITY_ORDER => JsString("LIABILITY_ORDER")
      case ApplicationType.CORRESPONDENCE  => JsString("CORRESPONDENCE")
      case ApplicationType.MISCELLANEOUS   => JsString("MISCELLANEOUS")
    }
  )
