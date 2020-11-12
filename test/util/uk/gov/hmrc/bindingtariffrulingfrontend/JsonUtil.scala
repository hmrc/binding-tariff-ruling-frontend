/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend

import play.api.libs.json.{JsArray, JsObject, JsValue}

object JsonUtil {

  def jsonPath(jsValue: JsValue, path: String*): Option[JsValue] = {
    val initial: Option[JsValue] = Some(jsValue)
    path.foldLeft(initial)((current: Option[JsValue], key: String) => current.flatMap(get(_, key)))
  }

  private def get(jsValue: JsValue, key: String): Option[JsValue] =
    jsValue match {
      case array: JsArray if key forall Character.isDigit =>
        Option(array.as[JsArray].value(key.toInt))
      case obj: JsObject =>
        obj.as[JsObject].value.get(key)
      case _ => None
    }

}
