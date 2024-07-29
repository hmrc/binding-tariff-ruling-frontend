/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.bindingtariffrulingfrontend.filters

import com.digitaltangible.playguard.RateLimitActionFilter
import com.digitaltangible.ratelimit.RateLimiter
import javax.inject.Inject
import play.api.mvc.{ActionFilter, Request, Result, Results}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import scala.concurrent.{ExecutionContext, Future}

class RateLimitFilter @Inject() (appConfig: AppConfig)(implicit val executionContext: ExecutionContext)
    extends ActionFilter[Request] {
  private val TrueClientIP = "True-Client-IP"

  private lazy val filter =
    new RateLimitActionFilter[Request](
      rateLimiter = new RateLimiter(appConfig.rateLimitBucketSize, appConfig.rateLimitRatePerSecond.toFloat, "IP")
    ) {

      override def keyFromRequest[A](implicit request: Request[A]): Any =
        request.headers.get(TrueClientIP).getOrElse(request.remoteAddress)

      override def rejectResponse[A](implicit request: Request[A]): Future[Result] =
        Future.successful(Results.TooManyRequests)
    }

  override protected def filter[A](request: Request[A]): Future[Option[Result]] =
    if (appConfig.rateLimiterEnabled) filter.filter(request) else Future.successful(None)
}
