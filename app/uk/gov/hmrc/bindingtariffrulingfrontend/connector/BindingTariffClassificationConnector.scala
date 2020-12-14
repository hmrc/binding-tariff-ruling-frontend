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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.Case
import uk.gov.hmrc.bindingtariffrulingfrontend.metrics.HasMetrics
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext
import com.kenshoo.play.metrics.Metrics

@Singleton
class BindingTariffClassificationConnector @Inject() (
  appConfig: AppConfig,
  client: AuthenticatedHttpClient,
  val metrics: Metrics
)(
  implicit ec: ExecutionContext
) extends InjectAuthHeader
    with HasMetrics {

  def get(reference: String)(implicit hc: HeaderCarrier): Future[Option[Case]] =
    withMetricsTimerAsync("get-case") { _ =>
      val url = s"${appConfig.bindingTariffClassificationUrl}/cases/$reference"
      client.GET[Option[Case]](url)
    }

}
