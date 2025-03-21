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

package uk.gov.hmrc.bindingtariffrulingfrontend.repository

import org.mongodb.scala.{MongoCollection, ReadConcern}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Helpers.*
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import org.mongodb.scala.SingleObservableFuture

import java.time.*
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global

class RulingMongoRepositoryTest
    extends AnyWordSpecLike
    with GuiceOneAppPerSuite
    with Matchers
    with DefaultPlayMongoRepositorySupport[Ruling] {

  private val clock = Clock.tickSeconds(ZoneOffset.UTC)

  override protected val repository: RulingMongoRepository = new RulingMongoRepository(mongoComponent)

  lazy val readConcern: ReadConcern = ReadConcern.MAJORITY

  protected def collection: MongoCollection[Ruling] = repository.collection

  val startOfToday: LocalDateTime = LocalDate.now().atStartOfDay
  val zoneOffsetToday: ZoneOffset = ZoneId.of("Europe/London").getRules.getOffset(startOfToday)
  val today: Instant              = startOfToday.toInstant(zoneOffsetToday)

  val startOfTomorrow: LocalDateTime  = LocalDate.now().plusDays(1).atStartOfDay
  val startOfNextMonth: LocalDateTime = LocalDate.now().plusMonths(1).atStartOfDay

  val zoneOffsetTomorrow: ZoneOffset  = ZoneId.of("Europe/London").getRules.getOffset(startOfTomorrow)
  val zoneOffsetNextMonth: ZoneOffset = ZoneId.of("Europe/London").getRules.getOffset(startOfNextMonth)

  val tomorrow: Instant  = startOfTomorrow.toInstant(zoneOffsetTomorrow)
  val nextMonth: Instant = startOfNextMonth.toInstant(zoneOffsetNextMonth)

  private def givenAnExistingDocument(ruling: Ruling): Unit =
    await(repository.update(ruling, upsert = true))

  private def thenTheDocumentCountShouldBe(count: Int): Unit =
    await(repository.collection.countDocuments().toFuture()) shouldBe count

  "Update" should {

    "Update One" in {
      val document = Ruling(reference = "ref", "code", clock.instant(), tomorrow, "justification", "description")
      givenAnExistingDocument(document)

      val update = document.copy(bindingCommodityCode = "code")

      await(repository.update(update, upsert = false)) shouldBe update
    }
  }

  "Delete by Reference" should {

    "Delete One" in {
      givenAnExistingDocument(
        Ruling(reference = "ref1", "code", clock.instant(), tomorrow, "justification", "description")
      )
      givenAnExistingDocument(
        Ruling(reference = "ref2", "code", clock.instant(), tomorrow, "justification", "description")
      )

      await(repository.delete("ref1"))
      thenTheDocumentCountShouldBe(1)
    }
  }

  "Delete Many" should {

    "Delete All" in {
      givenAnExistingDocument(
        Ruling(reference = "ref1", "code", clock.instant(), tomorrow, "justification", "description")
      )
      givenAnExistingDocument(
        Ruling(reference = "ref2", "code", clock.instant(), tomorrow, "justification", "description")
      )

      await(repository.deleteAll())

      thenTheDocumentCountShouldBe(0)
    }
  }

  "Get by Reference" should {

    "Retrieve None" in {
      await(repository.get("some id")) shouldBe None
    }

    "Retrieve One" in {
      val document = Ruling(reference = "ref", "code", clock.instant(), tomorrow, "justification", "description")
      givenAnExistingDocument(document)

      await(repository.get("ref")) shouldBe Some(document)
    }
  }

  "Get by SimpleSearch" should {

    "Retrieve None" in {
      await(repository.get(SimpleSearch(Some("ref"), imagesOnly = false, 1, 100))).results shouldBe Seq.empty
    }

    "Retrieve Multiple - no query" in {

      val document1 = Ruling(reference = "ref1", "0", clock.instant(), tomorrow, "justification", "exacting")
      val document2 = Ruling(reference = "ref2", "0", clock.instant(), tomorrow, "justification", "exactly")
      val document3 = Ruling(reference = "ref3", "0", clock.instant(), tomorrow, "justification", "fountain pen")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)
      givenAnExistingDocument(document3)

      val result = await(repository.get(SimpleSearch(Some("0"), imagesOnly = false, 1, 100))).results
      result should contain theSameElementsAs Seq(document3, document2, document1)
    }

    "Sort Rulings based on effectiveEndDate - latest end date first, earliest end date last" in {
      val document1 =
        Ruling(reference = "ref1", "0", clock.instant(), tomorrow.plusSeconds(2), "justification", "exacting")
      val document2 =
        Ruling(reference = "ref2", "0", clock.instant(), tomorrow.plusSeconds(1), "justification", "exactly")
      val document3 =
        Ruling(reference = "ref3", "0", clock.instant(), tomorrow.plusSeconds(3), "justification", "fountain pen")

      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)
      givenAnExistingDocument(document3)

      val result = await(repository.get(SimpleSearch(Some("0"), imagesOnly = false, 1, 100))).results
      result shouldBe Seq(document3, document1, document2)
    }

    "Retrieve One - by Reference - exact match" in {
      val document1 = Ruling(reference = "ref1", "0", clock.instant(), tomorrow, "justification", "description")
      val document2 = Ruling(reference = "ref11", "0", clock.instant(), tomorrow, "justification", "description")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch(Some("ref1"), imagesOnly = false, 1, 100))).results shouldBe Seq(document1)
    }

    "Retrieve One - by Commodity Code - starts with" in {
      val document1 = Ruling(reference = "ref1", "00", clock.instant(), tomorrow, "justification", "description")
      val document2 = Ruling(reference = "ref2", "10", clock.instant(), tomorrow, "justification", "description")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch(Some("0"), imagesOnly = false, 1, 100))).results shouldBe Seq(document1)
    }

    "Retrieve One - by Goods Description - case insensitive" in {
      val document1 = Ruling(reference = "ref1", "0", clock.instant(), tomorrow, "justification", "fountain pen")
      val document2 = Ruling(reference = "ref2", "0", clock.instant(), tomorrow, "justification", "laptop")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch(Some("FOUNTAIN"), imagesOnly = false, 1, 100))).results shouldBe Seq(document1)
      await(repository.get(SimpleSearch(Some("lapTOP"), imagesOnly = false, 1, 100))).results   shouldBe Seq(document2)
    }

    "Retrieve Multiple - by Goods Description - word stems" in {

      val document1 = Ruling(reference = "ref1", "0", clock.instant(), nextMonth, "justification", "exacting")
      val document2 = Ruling(reference = "ref2", "0", clock.instant(), tomorrow, "justification", "exactly")
      val document3 = Ruling(reference = "ref3", "0", clock.instant(), tomorrow, "justification", "fountain pen")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)
      givenAnExistingDocument(document3)

      await(repository.get(SimpleSearch(Some("exact"), imagesOnly = false, 1, 100))).results shouldBe
        Seq(document1, document2)
    }

    "Retrieve One - by Goods Description - images only" in {
      val document1 =
        Ruling(
          reference = "ref1",
          bindingCommodityCode = "0",
          effectiveStartDate = clock.instant(),
          effectiveEndDate = tomorrow,
          justification = "justification",
          goodsDescription = "exacting",
          images = Seq("id1, id2")
        )

      val document2 = Ruling(reference = "ref2", "0", clock.instant(), tomorrow, "justification", "exactly")
      val document3 = Ruling(reference = "ref3", "0", clock.instant(), tomorrow, "justification", "fountain pen")

      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)
      givenAnExistingDocument(document3)

      await(repository.get(SimpleSearch(Some("exact"), imagesOnly = true, 1, 100))).results shouldBe Seq(document1)
    }
  }
}
