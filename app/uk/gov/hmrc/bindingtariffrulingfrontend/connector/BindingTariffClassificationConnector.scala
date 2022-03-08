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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import com.kenshoo.play.metrics.Metrics
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.ApplicationType.ApplicationType
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.CaseStatus._
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.{ApplicationType, Case}
import uk.gov.hmrc.bindingtariffrulingfrontend.metrics.HasMetrics
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Paged.format
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Pagination}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BindingTariffClassificationConnector @Inject() (
  appConfig: AppConfig,
  client: AuthenticatedHttpClient,
  val metrics: Metrics
)(
  implicit ec: ExecutionContext
) extends HasMetrics with InjectAuthHeader {

  private lazy val completedStatus: String = COMPLETED.toString

  private lazy val cancelStatus: String = CANCELLED.toString

  private def buildQueryUrl(
    types: Seq[ApplicationType] = Seq(ApplicationType.BTI),
    statuses: String,
    minDecisionStart: Option[Instant],
    minDecisionEnd: Option[Instant],
    pagination: Pagination
  ): String = {
    val queryString =
      s"application_type=${types.map(_.toString).mkString(",")}" +
        s"&status=$statuses" +
        minDecisionStart.map(decisionStart => s"&min_decision_start=$decisionStart").getOrElse("") +
        minDecisionEnd.map(decisionEnd => s"&min_decision_end=$decisionEnd").getOrElse("") +
        s"&page=${pagination.pageIndex}" +
        s"&page_size=${pagination.pageSize}"
    s"${appConfig.bindingTariffClassificationUrl}/cases?$queryString"
  }

  def get(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] =
    withMetricsTimerAsync("get-case") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/$reference"
      client.GET[Option[Case]](url, headers = authHeaders(appConfig))
    }

  def newApprovedRulings(minDecisionStart: Instant, pagination: Pagination)(
    implicit hc: HeaderCarrier
  ): Future[Paged[Case]] =
    withMetricsTimerAsync("get-new-approved-rulings") { _ =>
      val url = buildQueryUrl(
        statuses         = completedStatus,
        minDecisionStart = Some(minDecisionStart),
        minDecisionEnd   = None,
        pagination       = pagination
      )
      client.GET[Paged[Case]](url, headers = authHeaders(appConfig))
    }

  def newCanceledRulings(minDecisionEnd: Instant, pagination: Pagination)(
    implicit hc: HeaderCarrier
  ): Future[Paged[Case]] =
    withMetricsTimerAsync("get-new-cancelled-rulings") { _ =>
      val url = buildQueryUrl(
        statuses         = cancelStatus,
        minDecisionStart = None,
        minDecisionEnd   = Some(minDecisionEnd),
        pagination       = pagination
      )
      client.GET[Paged[Case]](url, headers = authHeaders(appConfig))
    }
}
