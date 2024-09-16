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

package uk.gov.hmrc.bindingtariffrulingfrontend.workers

import org.apache.pekko.Done
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.scaladsl.Sink
import org.apache.pekko.stream.{ActorAttributes, Materializer, Supervision}
import play.api.Logging
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.{BindingTariffClassificationConnector, InjectAuthHeader}
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Pagination, SimplePagination}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.{MongoLockRepository, TimePeriodLockService}

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class RulingsWorker @Inject() (
  appConfig: AppConfig,
  bindingTariffClassificationConnector: BindingTariffClassificationConnector,
  rulingService: RulingService,
  mongoLockRepository: MongoLockRepository
)(implicit system: ActorSystem, mat: Materializer)
    extends InjectAuthHeader
    with Logging {

  implicit val ec: ExecutionContext = system.dispatchers.lookup("rulings-worker")
  implicit val hc: HeaderCarrier    = HeaderCarrier(extraHeaders = authHeaders(appConfig)(HeaderCarrier()))

  val StreamPageSize                       = 1000
  private val StreamPagination: Pagination = SimplePagination(pageSize = StreamPageSize)

  private val myLock: TimePeriodLockService =
    TimePeriodLockService(mongoLockRepository, lockId = "rulings-worker-lock", ttl = 2.minutes)

  private val decider: Supervision.Decider = {
    case NonFatal(e) =>
      logger.error("[RulingsWorker][decider] Skipping RulingsWorker updates due to error", e)
      Supervision.resume
    case _ =>
      Supervision.stop
  }

  def updateNewRulings(minDecisionStart: Instant)(implicit hc: HeaderCarrier): Future[Done] =
    Paged
      .stream(StreamPagination)(pagination =>
        bindingTariffClassificationConnector.newApprovedRulings(minDecisionStart, pagination)
      )
      .throttle(10, 1.second)
      .mapAsync(Runtime.getRuntime.availableProcessors()) { c =>
        logger.info(s"[RulingsWorker][updateNewRulings] Refreshing ruling with reference: ${c.reference}")
        myLock.withRenewedLock {
          rulingService.refresh(c.reference, Some(c))
        }
      }
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(Sink.ignore)

  def updateCancelledRulings(minDecisionEnd: Instant)(implicit hc: HeaderCarrier): Future[Done] =
    Paged
      .stream(StreamPagination)(pagination =>
        bindingTariffClassificationConnector.newCanceledRulings(minDecisionEnd, pagination)
      )
      .throttle(10, 1.second)
      .mapAsync(Runtime.getRuntime.availableProcessors()) { c =>
        logger.info(
          s"[RulingsWorker][updateCancelledRulings] Refreshing cancelled ruling with reference: ${c.reference}"
        )
        myLock.withRenewedLock {
          rulingService.refresh(c.reference, Some(c))
        }
      }
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(Sink.ignore)

}
