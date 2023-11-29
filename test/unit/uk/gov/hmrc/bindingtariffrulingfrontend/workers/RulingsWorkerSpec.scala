/*
 * Copyright 2023 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterAll
import org.mockito.MockitoSugar
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model._
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Pagination, SimplePagination}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.{Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future
import scala.concurrent.duration.Duration

//scalastyle:off magic.number
class RulingsWorkerSpec extends BaseSpec with MockitoSugar with BeforeAndAfterAll with MongoSupport { self =>

  private val rulingService = mock[RulingService]
  private val connector     = mock[BindingTariffClassificationConnector]
  private val appConfig     = mock[AppConfig]
  private val lockRepo      = mock[MongoLockRepository]

  val rulingWorker: RulingsWorker = new RulingsWorker(appConfig, connector, rulingService, lockRepo)(ac, mat)

  val StreamPageSize         = 50
  val pagination: Pagination = SimplePagination(pageSize = StreamPageSize)

  val startDate: Instant           = LocalDate.of(2017, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
  val endDate: Instant             = LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
  val validDecision: Decision      = Decision("code", Some(startDate), Some(endDate), "justification", "description")
  val publicAttachment: Attachment = Attachment("file-id", public = true, shouldPublishToRulings = true)
  val validCase: Case = Case(
    reference   = "ref",
    status      = CaseStatus.CANCELLED,
    application = Application(`type` = ApplicationType.BTI),
    decision    = Some(validDecision),
    attachments = Seq(publicAttachment),
    keywords    = Set("keyword")
  )

  val pagedNewCases: Paged[Case] = Paged(
    results =
      Seq(validCase.copy(reference = "ref1"), validCase.copy(reference = "ref2"), validCase.copy(reference = "ref3")),
    pagination  = pagination,
    resultCount = 3
  )

  val pagedCanceledCases: Paged[Case] = Paged(
    results     = Seq(validCase.copy(reference = "ref4"), validCase.copy(reference = "ref5")),
    pagination  = pagination,
    resultCount = 2
  )

  override protected def beforeAll(): Unit = {
    given(lockRepo.refreshExpiry(any[String], any[String], any[Duration]))
      .willReturn(Future.successful(true))
    given(lockRepo.takeLock(any[String], any[String], any[Duration]))
      .willReturn(Future.successful(None))
    given(lockRepo.releaseLock(any[String], any[String]))
      .willReturn(Future.successful(()))
    given(connector.newApprovedRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
      .willReturn(Future.successful(pagedNewCases))
    given(connector.newCanceledRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
      .willReturn(Future.successful(pagedCanceledCases))
  }

  "updateNewRulings" should {
    "get new rulings from the backend and delegate to the RulingService" in {
      given(connector.newApprovedRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
        .willReturn(pagedNewCases)
        .willReturn(Future.successful(Paged.empty[Case]))
      given(rulingService.refresh(any[String], any[Some[Case]])(any[HeaderCarrier])).willReturn(Future.successful(()))

      await(rulingWorker.updateNewRulings(startDate)) shouldBe Done
      verify(rulingService).refresh(refEq("ref1"), any[Some[Case]])(any[HeaderCarrier])
      verify(rulingService).refresh(refEq("ref2"), any[Some[Case]])(any[HeaderCarrier])
      verify(rulingService).refresh(refEq("ref3"), any[Some[Case]])(any[HeaderCarrier])
    }

  }

  "updateCanceledRulings" should {
    "get canceled rulings from the backend and delegate to the RulingService" in {
      given(connector.newCanceledRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
        .willReturn(pagedCanceledCases)
        .willReturn(Future.successful(Paged.empty[Case]))
      given(rulingService.refresh(any[String], any[Some[Case]])(any[HeaderCarrier])).willReturn(Future.successful(()))

      await(rulingWorker.updateCancelledRulings(endDate)) shouldBe Done
      verify(rulingService).refresh(refEq("ref4"), any[Some[Case]])(any[HeaderCarrier])
      verify(rulingService).refresh(refEq("ref5"), any[Some[Case]])(any[HeaderCarrier])
    }
  }

}
