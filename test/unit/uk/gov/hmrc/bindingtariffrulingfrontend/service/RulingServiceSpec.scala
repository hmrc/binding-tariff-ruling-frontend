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

package uk.gov.hmrc.bindingtariffrulingfrontend.service

import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{refEq, _}
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

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.collection.immutable.ListMap
import scala.concurrent.Future

class RulingServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val connector        = mock[BindingTariffClassificationConnector]
  private val repository       = mock[RulingRepository]
  private val fileStoreService = mock[FileStoreService]
  private val auditService     = mock[AuditService]

  private val service = new RulingService(repository, auditService, fileStoreService, connector)

  override protected def afterEach(): Unit = {
    super.afterEach()
    reset(repository, connector, auditService, fileStoreService)
  }

  "Service DELETE ALL" should {

    "delegate to repository" in {
      given(repository.deleteAll()) willReturn Future.successful(())
      await(service.deleteAll()) shouldBe ((): Unit)
      verifyNoInteractions(auditService)
    }
  }

  "Service DELETE" should {

    "delegate to repository" in {
      given(repository.delete(refEq("ref"))) willReturn Future.successful(())
      await(service.delete("ref")) shouldBe ((): Unit)
      verifyNoInteractions(auditService)
    }
  }

  "Service GET by reference" should {

    "delegate to repository" in {
      given(repository.get("id")) willReturn Future.successful(None)
      await(service.get("id")) shouldBe None
      verifyNoInteractions(auditService)
    }
  }

  "Service GET by search" should {
    val search = SimpleSearch(Some("query"), imagesOnly = false, 1, 1)

    "delegate to repository" in {
      given(repository.get(search)) willReturn Future.successful(Paged.empty[Ruling])
      await(service.get(search)) shouldBe Paged.empty[Ruling]
      verifyNoInteractions(auditService)
    }
  }

  "Service Refresh" should {
    val startDate = Instant.now().plus(10, ChronoUnit.SECONDS)
    val endDate   = Instant.now()

    val validDecision = Decision("code", Some(startDate), Some(endDate), "justification", "description")

    val publicAttachment       = Attachment("public-file-id", public     = true, shouldPublishToRulings  = true)
    val publicImage            = Attachment("public-image-id", public    = true, shouldPublishToRulings  = true)
    val privateAttachment      = Attachment("private-file-id", public    = false, shouldPublishToRulings = false)
    val privateImage           = Attachment("private-image-id", public   = false, shouldPublishToRulings = false)
    val nonPublishAttachment   = Attachment("nopublish-file-id", public  = true, shouldPublishToRulings  = false)
    val nonPublishImage        = Attachment("nopublish-image-id", public = true, shouldPublishToRulings  = false)
    val invalidStateAttachment = Attachment("invalid-file-id", public    = false, shouldPublishToRulings = true)
    val invalidStateImage      = Attachment("invalid-image-id", public   = false, shouldPublishToRulings = true)

    val attachments = Seq(
      publicAttachment,
      publicImage,
      privateAttachment,
      privateImage,
      nonPublishAttachment,
      nonPublishImage,
      invalidStateAttachment,
      invalidStateImage
    )

    val validCase: Case = Case(
      reference   = "ref",
      status      = CaseStatus.COMPLETED,
      application = Application(`type` = ApplicationType.BTI),
      decision    = Some(validDecision),
      attachments = attachments,
      keywords    = Set("keyword")
    )

    val fileMetadata = ListMap(
      "public-file-id" -> FileMetadata(
        "public-file-id",
        Some("some.pdf"),
        Some("application/pdf"),
        Some("https://foo")
      ),
      "public-image-id" -> FileMetadata(
        "public-image-id",
        Some("some.png"),
        Some("image/png"),
        Some("https://bar")
      ),
      "private-file-id" -> FileMetadata(
        "private-file-id",
        Some("some.txt"),
        Some("text/plain"),
        Some("https://baz")
      ),
      "private-image-id" -> FileMetadata(
        "private-image-id",
        Some("some.jpeg"),
        Some("application/jpeg"),
        Some("https://quu")
      ),
      "nopublish-attachment-id" -> FileMetadata(
        "nopublish-attachment-id",
        Some("some.txt"),
        Some("text/plain"),
        Some("https://quux")
      ),
      "nopublish-image-id" -> FileMetadata(
        "nopublish-image-id",
        Some("some.jpeg"),
        Some("application/jpeg"),
        Some("https://arg")
      ),
      "invalid-attachment-id" -> FileMetadata(
        "invalid-attachment-id",
        Some("some.txt"),
        Some("text/plain"),
        Some("https://blarg")
      ),
      "invalid-image-id" -> FileMetadata(
        "invalid-image-id",
        Some("some.jpeg"),
        Some("application/jpeg"),
        Some("https://flurg")
      )
    )

    "do nothing when case doesn't exist in repository or connector" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(None)

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyNoInteractions(auditService)
    }

    "create new ruling" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(Some(validCase))
      given(repository.update(any[Ruling], any[Boolean])) will returnTheRuling
      given(fileStoreService.get(attachments.map(_.id))).willReturn(fileMetadata)

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository).update(any[Ruling], refEq(true))

      val expectedRuling = Ruling(
        validCase.reference,
        validCase.decision.get.bindingCommodityCode,
        startDate,
        endDate,
        validCase.decision.get.justification,
        validCase.decision.get.goodsDescription,
        validCase.keywords,
        Seq(publicAttachment.id),
        Seq(publicImage.id)
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
      given(fileStoreService.get(attachments.map(_.id))).willReturn(fileMetadata)

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository).update(any[Ruling], refEq(false))

      val expectedRuling = Ruling(
        validCase.reference,
        validCase.decision.get.bindingCommodityCode,
        startDate,
        endDate,
        validCase.decision.get.justification,
        validCase.decision.get.goodsDescription,
        validCase.keywords,
        Seq(publicAttachment.id),
        Seq(publicImage.id)
      )
      theRulingUpdated shouldBe expectedRuling

      verifyNoInteractions(auditService)
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
      verifyNoInteractions(auditService)
    }

    "filter cases not BTT" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(
        Some(validCase.copy(application = Application(`type` = ApplicationType.LIABILITY_ORDER)))
      )

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyNoInteractions(auditService)
    }

    "filter cases without Decision" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(Some(validCase.copy(decision = None)))

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyNoInteractions(auditService)
    }

    "filter cases without Decision Start Date" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(
        Some(validCase.copy(decision = Some(validDecision.copy(effectiveStartDate = None))))
      )

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyNoInteractions(auditService)
    }

    "filter cases without Decision End Date" in {
      given(repository.get("ref")) willReturn Future.successful(None)
      given(connector.get("ref")) willReturn Future.successful(
        Some(validCase.copy(decision = Some(validDecision.copy(effectiveEndDate = None))))
      )

      await(service.refresh("ref")) shouldBe ((): Unit)

      verify(repository, never()).update(any[Ruling], anyBoolean())
      verifyNoInteractions(auditService)
    }


    def theRulingUpdated: Ruling = {
      val captor = ArgumentCaptor.forClass(classOf[Ruling])
      verify(repository).update(captor.capture(), anyBoolean())
      captor.getValue
    }

    def returnTheRuling: Answer[Future[Ruling]] = (invocation: InvocationOnMock) => Future.successful(invocation.getArgument(0))

  }



}
