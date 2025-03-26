/*
 * Copyright 2025 HM Revenue & Customs
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
import org.apache.pekko.stream.{Materializer, Supervision}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{mock, reset, verify, when}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import play.api.libs.json.Json
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.*
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Pagination, SimplePagination}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.mongo.test.MongoSupport

import java.time.{Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class RulingsWorkerSpec extends BaseSpec with BeforeAndAfterAll with BeforeAndAfterEach with MongoSupport { self =>

  private val rulingService = mock(classOf[RulingService])
  private val connector     = mock(classOf[BindingTariffClassificationConnector])
  private val appConfig     = mock(classOf[AppConfig])
  private val lockRepo      = mock(classOf[MongoLockRepository])

  val rulingWorker: RulingsWorker = new RulingsWorker(appConfig, connector, rulingService, lockRepo)(ac, mat)

  val StreamPageSize         = 50
  val pagination: Pagination = SimplePagination(pageSize = StreamPageSize)

  val startDate: Instant           = LocalDate.of(2017, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
  val endDate: Instant             = LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
  val validDecision: Decision      = Decision("code", Some(startDate), Some(endDate), "justification", "description")
  val publicAttachment: Attachment = Attachment("file-id", public = true, shouldPublishToRulings = true)
  val validCase: Case = Case(
    reference = "ref",
    status = CaseStatus.CANCELLED,
    application = Application(`type` = ApplicationType.BTI),
    decision = Some(validDecision),
    attachments = Seq(publicAttachment),
    keywords = Set("keyword")
  )

  val pagedNewCases: Paged[Case] = Paged(
    results =
      Seq(validCase.copy(reference = "ref1"), validCase.copy(reference = "ref2"), validCase.copy(reference = "ref3")),
    pagination = pagination,
    resultCount = 3
  )

  val pagedCanceledCases: Paged[Case] = Paged(
    results = Seq(validCase.copy(reference = "ref4"), validCase.copy(reference = "ref5")),
    pagination = pagination,
    resultCount = 2
  )

  override protected def beforeAll(): Unit = {
    when(lockRepo.refreshExpiry(any[String], any[String], any[Duration]))
      .thenReturn(Future.successful(true))
    when(lockRepo.takeLock(any[String], any[String], any[Duration]))
      .thenReturn(Future.successful(None))
    when(lockRepo.releaseLock(any[String], any[String]))
      .thenReturn(Future.successful(()))
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector)
  }

  "updateNewRulings" should {
    "get new rulings from the backend and delegate to the RulingService" in {
      when(connector.newApprovedRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
        .thenReturn(Future.successful(pagedNewCases))
        .thenReturn(Future.successful(Paged.empty[Case]))
      when(rulingService.refresh(any[String], any[Some[Case]])(any[HeaderCarrier])).thenReturn(Future.successful(()))

      await(rulingWorker.updateNewRulings(startDate)) shouldBe Done
      verify(rulingService).refresh(refEq("ref1"), any[Some[Case]])(any[HeaderCarrier])
      verify(rulingService).refresh(refEq("ref2"), any[Some[Case]])(any[HeaderCarrier])
      verify(rulingService).refresh(refEq("ref3"), any[Some[Case]])(any[HeaderCarrier])
    }

  }

  "updateCanceledRulings" should {
    "get canceled rulings from the backend and delegate to the RulingService" in {
      when(connector.newCanceledRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
        .thenReturn(Future.successful(pagedCanceledCases))
        .thenReturn(Future.successful(Paged.empty[Case]))
      when(rulingService.refresh(any[String], any[Some[Case]])(any[HeaderCarrier])).thenReturn(Future.successful(()))

      await(rulingWorker.updateCancelledRulings(endDate)) shouldBe Done
      verify(rulingService).refresh(refEq("ref4"), any[Some[Case]])(any[HeaderCarrier])
      verify(rulingService).refresh(refEq("ref5"), any[Some[Case]])(any[HeaderCarrier])
    }
  }

  "Case object" should {
    "serialize and deserialize using JSON formatter" in {
      val json = Json.toJson(validCase)(Case.format)

      (json \ "reference").as[String] shouldBe validCase.reference
      (json \ "status").as[String]    shouldBe validCase.status.toString

      val caseFromJson = json.as[Case](Case.format)

      caseFromJson shouldBe validCase
    }
  }

  "RulingsWorker decider" should {
    "handle NonFatal exceptions by resuming" in {
      implicit val system   = ActorSystem("test-system")
      implicit val mat      = Materializer(system)
      val mockAppConfig     = mock(classOf[AppConfig])
      val mockConnector     = mock(classOf[BindingTariffClassificationConnector])
      val mockRulingService = mock(classOf[RulingService])
      val mockMongoLockRepo = mock(classOf[MongoLockRepository])

      val worker = new RulingsWorker(
        mockAppConfig,
        mockConnector,
        mockRulingService,
        mockMongoLockRepo
      )

      val deciderField = worker.getClass.getDeclaredField("decider")
      deciderField.setAccessible(true)
      val decider = deciderField.get(worker).asInstanceOf[Supervision.Decider]

      val nonFatalException = new RuntimeException("Test exception")
      val nonFatalResult    = decider(nonFatalException)
      nonFatalResult shouldBe Supervision.Resume

      val fatalException = new OutOfMemoryError("Test OOM")
      val fatalResult    = decider(fatalException)
      fatalResult shouldBe Supervision.Stop

      system.terminate()
    }
  }

}
