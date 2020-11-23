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

package uk.gov.hmrc.bindingtariffrulingfrontend.service

import java.time.Instant
import java.time.temporal.ChronoUnit

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito.given
import org.mockito.Mockito._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.bindingtariffrulingfrontend.audit.AuditService
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.BindingTariffClassificationConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model._
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.RulingRepository

import scala.concurrent.Future

class RulingServiceTest extends BaseSpec with BeforeAndAfterEach {

  private val connector    = mock[BindingTariffClassificationConnector]
  private val repository   = mock[RulingRepository]
  private val auditService = mock[AuditService]

  private val service = new RulingService(repository, auditService, connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(repository, connector, auditService)
  }

  "Service DELETE" should {

    "delegate to repository" in {
      given(repository.delete()) willReturn Future.successful(())
      await(service.delete()) shouldBe ((): Unit)
      verifyZeroInteractions(auditService)
    }
  }

  "Service GET by reference" should {

    "delegate to repository" in {
      given(repository.get("id")) willReturn Future.successful(None)
      await(service.get("id")) shouldBe None
      verifyZeroInteractions(auditService)
    }
  }

  "Service GET by search" should {
    val search = SimpleSearch(Some("query"), imagesOnly = false, 1, 1)

    "delegate to repository" in {
      given(repository.get(search)) willReturn Future.successful(Paged.empty[Ruling])
      await(service.get(search)) shouldBe Paged.empty[Ruling]
      verifyZeroInteractions(auditService)
    }
  }

  "Service Refresh" should {
    val startDate         = Instant.now().plus(10, ChronoUnit.SECONDS)
    val endDate           = Instant.now()
    val validDecision     = Decision("code", Some(startDate), Some(endDate), "justification", "description")
    val publicAttachment  = Attachment("file-id", public = true)
    val privateAttachment = Attachment("file-id", public = false)
    val validCase: Case = Case(
      reference   = "ref",
      status      = CaseStatus.COMPLETED,
      application = Application(`type` = ApplicationType.BTI),
      decision    = Some(validDecision),
      attachments = Seq(publicAttachment, privateAttachment),
      keywords    = Set("keyword")
    )

    "do nothing when case doesn't exist in repository or connector" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(None)

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyZeroInteractions(auditService)
    }

    "create new ruling" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(Some(validCase))
      given(repository.update(any[Ruling], any[Boolean])) will returnTheRuling

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository).update(any[Ruling], refEq(true))
      val expectedRuling = Ruling(
        "ref",
        "code",
        startDate,
        endDate,
        "justification",
        "description",
        Set("keyword"),
        Seq("file-id")
      )
      theRulingUpdated shouldBe expectedRuling

      verify(auditService).auditRulingCreated(expectedRuling)(hc)
      verifyNoMoreInteractions(auditService)
    }

    "update existing ruling" in {
      val existing = Ruling("ref", "old", Instant.now, Instant.now, "old", "old", Set("old"), Seq("old"))
      given(repository.get("ref")) willReturn Future.successful(Some(existing))
      given(connector.get("ref")) willReturn Future.successful(Some(validCase))
      given(repository.update(any[Ruling], any[Boolean])) will returnTheRuling

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository).update(any[Ruling], refEq(false))

      val expectedRuling = Ruling(
        "ref",
        "code",
        startDate,
        endDate,
        "justification",
        "description",
        Set("keyword"),
        Seq("file-id")
      )
      theRulingUpdated shouldBe expectedRuling

      verifyZeroInteractions(auditService)
    }

    "delete existing ruling" in {
      val existing = Ruling("ref", "old", Instant.now, Instant.now, "old", "old", Set("old"), Seq("old"))
      given(repository.get("ref")) willReturn Future.successful(Some(existing))
      given(connector.get("ref")) willReturn Future.successful(None)
      given(repository.delete("ref")) willReturn Future.successful(())

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository).delete("ref")

      verify(auditService).auditRulingDeleted("ref")(hc)
      verifyNoMoreInteractions(auditService)
    }

    "filter cases not COMPLETED" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(Some(validCase.copy(status = CaseStatus.OPEN)))

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyZeroInteractions(auditService)
    }

    "filter cases not BTT" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(
        Some(validCase.copy(application = Application(`type` = ApplicationType.LIABILITY_ORDER)))
      )

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyZeroInteractions(auditService)
    }

    "filter cases without Decision" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(Some(validCase.copy(decision = None)))

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyZeroInteractions(auditService)
    }

    "filter cases without Decision Start Date" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(
        Some(validCase.copy(decision = Some(validDecision.copy(effectiveStartDate = None))))
      )

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyZeroInteractions(auditService)
    }

    "filter cases without Decision End Date" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(
        Some(validCase.copy(decision = Some(validDecision.copy(effectiveEndDate = None))))
      )

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyZeroInteractions(auditService)
    }

    def theRulingUpdated: Ruling = {
      val captor = ArgumentCaptor.forClass(classOf[Ruling])
      verify(repository).update(captor.capture(), anyBoolean())
      captor.getValue
    }

    def returnTheRuling: Answer[Future[Ruling]] = new Answer[Future[Ruling]] {
      override def answer(invocation: InvocationOnMock): Future[Ruling] = Future.successful(invocation.getArgument(0))
    }

  }

}
