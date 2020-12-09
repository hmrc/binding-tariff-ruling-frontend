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

package uk.gov.hmrc.bindingtariffrulingfrontend.repository

import java.time.Instant

import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.api.{DB, ReadConcern}
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext.Implicits.global
import java.time.temporal.ChronoUnit
import java.time.LocalDate
import java.time.ZoneId

class RulingMongoRepositoryTest
    extends MongoUnitSpec
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with MongoSpecSupport
    with Eventually {
  self =>

  import Ruling.Mongo.format

  private val provider: MongoDbProvider = new MongoDbProvider {
    override val mongo: () => DB = self.mongo
  }

  private def repository            = new RulingMongoRepository(provider)
  lazy val readConcern: ReadConcern = ReadConcern.Majority

  override protected def collection: JSONCollection = repository.collection

  val startOfToday = LocalDate.now().atStartOfDay
  val zoneOffsetToday = ZoneId.of("Europe/London").getRules().getOffset(startOfToday)
  val today = startOfToday.toInstant(zoneOffsetToday)

  val startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay
  val zoneOffsetTomorrow = ZoneId.of("Europe/London").getRules().getOffset(startOfTomorrow)
  val tomorrow = startOfTomorrow.toInstant(zoneOffsetTomorrow)

  override def beforeEach(): Unit = {
    super.beforeEach()
    await(repository.drop)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    await(repository.drop)
  }

  "Update" should {
    "Update One" in {
      val document = Ruling(reference = "ref", "code", Instant.now, tomorrow, "justification", "description")
      givenAnExistingDocument(document)

      val update = document.copy(bindingCommodityCode = "code")

      await(repository.update(update, upsert = false)) shouldBe update
    }
  }

  "Delete by Reference" should {
    "Delete One" in {
      givenAnExistingDocument(
        Ruling(reference = "ref1", "code", Instant.now, tomorrow, "justification", "description")
      )
      givenAnExistingDocument(
        Ruling(reference = "ref2", "code", Instant.now, tomorrow, "justification", "description")
      )

      await(repository.delete("ref1"))

      thenTheDocumentCountShouldBe(1)
    }
  }

  "Delete Many" should {
    "Delete All" in {
      givenAnExistingDocument(
        Ruling(reference = "ref1", "code", Instant.now, tomorrow, "justification", "description")
      )
      givenAnExistingDocument(
        Ruling(reference = "ref2", "code", Instant.now, tomorrow, "justification", "description")
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
      // Given
      val document = Ruling(reference = "ref", "code", Instant.now, tomorrow, "justification", "description")
      givenAnExistingDocument(document)

      await(repository.get("ref")) shouldBe Some(document)
    }
  }

  "Get by SimpleSearch" should {
    "Retrieve None" in {
      await(repository.get(SimpleSearch(Some("ref"), imagesOnly = false, 1, 100))).results shouldBe Seq.empty
    }

    "Retrieve One - by Reference - exact match" in {
      // Given
      val document1 = Ruling(reference = "ref1", "0", Instant.now, tomorrow, "justification", "description")
      val document2 = Ruling(reference = "ref11", "0", Instant.now, tomorrow, "justification", "description")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch(Some("ref1"), imagesOnly = false, 1, 100))).results shouldBe Seq(document1)
    }

    "Retrieve One - by Commodity Code - starts with" in {
      // Given
      val document1 = Ruling(reference = "ref1", "00", Instant.now, tomorrow, "justification", "description")
      val document2 = Ruling(reference = "ref2", "10", Instant.now, tomorrow, "justification", "description")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch(Some("0"), imagesOnly = false, 1, 100))).results shouldBe Seq(document1)
    }

    "Retrieve One - by Goods Description - case insensitive" in {
      // Given
      val document1 = Ruling(reference = "ref1", "0", Instant.now, tomorrow, "justification", "fountain pen")
      val document2 = Ruling(reference = "ref2", "0", Instant.now, tomorrow, "justification", "laptop")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch(Some("FOUNTAIN"), imagesOnly = false, 1, 100))).results shouldBe Seq(document1)
      await(repository.get(SimpleSearch(Some("lapTOP"), imagesOnly = false, 1, 100))).results shouldBe Seq(document2)
    }

    "Retrieve One - by Goods Description - word stems" in {
      // Given
      val document1 = Ruling(reference = "ref1", "0", Instant.now, tomorrow, "justification", "exacting")
      val document2 = Ruling(reference = "ref2", "0", Instant.now, tomorrow, "justification", "exactly")
      val document3 = Ruling(reference = "ref3", "0", Instant.now, tomorrow, "justification", "fountain pen")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)
      givenAnExistingDocument(document3)

      await(repository.get(SimpleSearch(Some("exact"), imagesOnly = false, 1, 100))).results shouldBe Seq(document1, document2)
    }
  }

  private def givenAnExistingDocument(ruling: Ruling): Unit =
    await(repository.collection.insert(ordered = false).one(ruling))

  private def thenTheDocumentCountShouldBe(count: Int): Unit =
    await(repository.collection.count(None, Some(0), 0, None, readConcern)) shouldBe count

}
