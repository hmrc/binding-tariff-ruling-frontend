/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.utils

import java.time.Instant

import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.ApplicationType
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.ApplicationType.ApplicationType
import uk.gov.hmrc.bindingtariffrulingfrontend.model.NoPagination

trait CaseQueueBuilder {

  def buildQueryUrl(
    types: Seq[ApplicationType] = Seq(ApplicationType.BTI),
    statuses: String,
    minDecisionStart: Option[Instant],
    minDecisionEnd: Option[Instant]

  ): String = {
    val sortBy = "application.type,application.status"
    val queryString = s"/cases?application_type=${types.map(_.toString).mkString(",")}&status=$statuses" +
      s"&min_decision_start=$minDecisionStart&min_decision_end=$minDecisionEnd&sort_by=$sortBy&sort_direction=desc&" +
      s"pagination=${NoPagination()}"
    queryString
  }

}
