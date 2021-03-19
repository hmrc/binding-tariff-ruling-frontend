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

import org.joda.time.Duration
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfterAll
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model._
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Pagination, SimplePagination}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.{LockRepoProvider, RulingRepository}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.lock.LockRepository
import uk.gov.hmrc.mongo.MongoSpecSupport

import java.time.{Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future

class RulingsWorkerSpec extends BaseSpec with MockitoSugar with BeforeAndAfterAll with MongoSpecSupport { self =>

  private val rulingService = mock[RulingService]
  private val connector     = mock[BindingTariffClassificationConnector]
  private val appConfig     = mock[AppConfig]
  private val lockRepo      = mock[LockRepository]
  private val repository    = mock[RulingRepository]

  val lockRepoProvider = new LockRepoProvider {
    def repo = () => lockRepo
  }

  val configuredApp: GuiceApplicationBuilder => GuiceApplicationBuilder =
    _.configure(
      "metrics.jvm"     -> false,
      "metrics.enabled" -> false
    ).overrides(
      bind[AppConfig].to(appConfig),
      bind[LockRepoProvider].to(lockRepoProvider),
      bind[BindingTariffClassificationConnector].to(connector),
      bind[RulingService].to(rulingService)
    )

  val rulingWorker: RulingsWorker = new RulingsWorker(appConfig, lockRepoProvider, connector, rulingService)(ac, mat)

  val StreamPageSize         = 50
  val pagination: Pagination = SimplePagination(pageSize = StreamPageSize)

  val startDate        = LocalDate.of(2017, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
  val endDate          = LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
  val validDecision    = Decision("code", Some(startDate), Some(endDate), "justification", "description")
  val publicAttachment = Attachment("file-id", public = true, shouldPublishToRulings = true)
  val validCase: Case = Case(
    reference   = "ref",
    status      = CaseStatus.CANCELLED,
    application = Application(`type` = ApplicationType.BTI),
    decision    = Some(validDecision),
    attachments = Seq(publicAttachment),
    keywords    = Set("keyword")
  )

  val pagedCases = Paged(
    results =
      Seq(validCase.copy(reference = "ref1"), validCase.copy(reference = "ref2"), validCase.copy(reference = "ref3")),
    pagination  = pagination,
    resultCount = 3
  )

  override protected def beforeAll(): Unit = {
    given(lockRepo.renew(any[String], any[String], any[Duration]))
      .willReturn(Future.successful(true))
    given(connector.newApprovedRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
      .willReturn(Future.successful(pagedCases))
  }

  val minDecisionStart = LocalDate.now().atStartOfDay().minusHours(12).toInstant(ZoneOffset.UTC)

  "updateNewRulings" should {
    "refresh ruling with reference and update it" in {
      given(lockRepo.renew(any[String], any[String], any[Duration]))
        .willReturn(Future.successful(false))
      given(connector.newApprovedRulings(any[Instant], any[Pagination])(any[HeaderCarrier]))
        .willReturn(pagedCases)

      //await(rulingWorker.updateNewRulings(any[Instant]) shouldBe ((): Unit))
    }
  }

//  "RulingsWorker" should {
//    "Acquire lock and updateNewRulings" in {
//      Helpers.running(configuredApp) { app =>
//        await(app.injector.instanceOf[RulingsWorker].updateNewRulings(minDecisionStart)(any[HeaderCarrier]))
//        verify(rulingService).refresh(refEq("ref1"))(any[HeaderCarrier])
//        verify(rulingService).refresh(refEq("ref2"))(any[HeaderCarrier])
//        verify(rulingService).refresh(refEq("ref3"))(any[HeaderCarrier])
//      }
//    }
//  }

}
