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

package uk.gov.hmrc.bindingtariffrulingfrontend.utils

import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.ApplicationType
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.ApplicationType.ApplicationType
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Pagination

import java.time.Instant

trait CaseQueueBuilder {

  def buildQueryUrl(
    types: Seq[ApplicationType] = Seq(ApplicationType.BTI),
    statuses: String,
    minDecisionStart: Option[Instant],
    minDecisionEnd: Option[Instant],
    pagination: Pagination
  ): String = {
    val queryString = s"/cases?application_type=${types.map(_.toString).mkString(",")}&status=$statuses" +
      minDecisionStart.map(decisionStart => s"&min_decision_start=$decisionStart").getOrElse("") +
      minDecisionEnd.map(decisionEnd => s"&min_decision_end=$decisionEnd").getOrElse("") +
      s"&page=${pagination.pageIndex}" +
      s"&page_size=${pagination.pageSize}"
    queryString
  }

}
