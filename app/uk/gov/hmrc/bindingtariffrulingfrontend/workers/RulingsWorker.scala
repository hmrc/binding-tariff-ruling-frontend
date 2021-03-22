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

package uk.gov.hmrc.bindingtariffrulingfrontend.workers

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import akka.stream.{ActorAttributes, Materializer, Supervision}
import org.joda.time.Duration
import play.api.Logging
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.{BindingTariffClassificationConnector, InjectAuthHeader}
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Pagination, SimplePagination}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.LockRepoProvider
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lock.ExclusiveTimePeriodLock

import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class RulingsWorker @Inject() (
  appConfig: AppConfig,
  lockRepo: LockRepoProvider,
  bindingTariffClassificationConnector: BindingTariffClassificationConnector,
  rulingService: RulingService
)(implicit system: ActorSystem, mat: Materializer)
    extends ExclusiveTimePeriodLock
    with InjectAuthHeader
    with Logging {

  implicit val ec: ExecutionContext = system.dispatchers.lookup("rulings-worker")
  implicit val hc: HeaderCarrier    = addAuth(appConfig, HeaderCarrier())

  val StreamPageSize               = 1000
  val StreamPagination: Pagination = SimplePagination(pageSize = StreamPageSize)
  val LocalDateFormatter           = DateTimeFormatter.ISO_LOCAL_DATE

  val repo        = lockRepo.repo()
  val lockId      = "rulings-worker-lock"
  val holdLockFor = Duration.standardMinutes(2)

  val decider: Supervision.Decider = {
    case NonFatal(e) =>
      logger.error("Skipping RulingsWorker updates due to error", e)
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
      .mapAsync(Runtime.getRuntime().availableProcessors()) { c =>
        logger.info(s"Refreshing ruling with reference: ${c.reference}")
        tryToAcquireOrRenewLock {
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
      .mapAsync(Runtime.getRuntime().availableProcessors()) { c =>
        logger.info(s"Refreshing cancelled ruling with reference: ${c.reference}")
        tryToAcquireOrRenewLock {
          rulingService.refresh(c.reference, Some(c))
        }
      }
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(Sink.ignore)

}
