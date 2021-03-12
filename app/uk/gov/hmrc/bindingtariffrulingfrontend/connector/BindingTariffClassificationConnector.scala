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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import com.kenshoo.play.metrics.Metrics
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.Case
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.CaseStatus._
import uk.gov.hmrc.bindingtariffrulingfrontend.metrics.HasMetrics
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Paged.format
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{NoPagination, Paged}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.time.{ZoneOffset, ZonedDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BindingTariffClassificationConnector @Inject() (
  appConfig: AppConfig,
  client: AuthenticatedHttpClient,
  val metrics: Metrics
)(
  implicit ec: ExecutionContext
) extends HasMetrics {

  private lazy val statuses: String = Set(CANCELLED, COMPLETED, ANNULLED)
    .map(_.toString)
    .mkString(",")

  def get(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] =
    withMetricsTimerAsync("get-case") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/$reference"
      client.GET[Option[Case]](url)
    }

  def newApprovedRulings(implicit hc: HeaderCarrier): Future[Paged[Case]] = {

    val time = ZonedDateTime.of(2019, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant

    val queryString =
      s"application_type=${"BTI"}&status=$statuses=&min_decision_end=$time&pagination=${NoPagination()}&page_size=${Integer.MAX_VALUE}"
    val url = s"${appConfig.bindingTariffClassificationUrl}/cases?$queryString"

    client.GET[Paged[Case]](url)
  }

  def newCanceledRulings(implicit hc: HeaderCarrier) = ???

}
