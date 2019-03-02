/*
 * Copyright 2019 HM Revenue & Customs
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
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import reactivemongo.api.DB
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import uk.gov.hmrc.mongo.MongoSpecSupport

import scala.concurrent.ExecutionContext.Implicits.global

class RulingMongoRepositoryTest extends MongoUnitSpec
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with MongoSpecSupport
  with Eventually
  with MockitoSugar {
  self =>

  private val provider: MongoDbProvider = new MongoDbProvider {
    override val mongo: () => DB = self.mongo
  }

  private val config = mock[AppConfig]

  private def repository = new RulingMongoRepository(config, provider)

  override protected def collection: JSONCollection = repository.collection

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
      val document = Ruling(reference = "ref", "code", Instant.now, Instant.now, "justification", "description")
      givenAnExistingDocument(document)

      val update = document.copy(bindingCommodityCode = "code")

      await(repository.update(update, upsert = false)) shouldBe update
    }
  }

  "Delete" should {
    "Delete One" in {
      givenAnExistingDocument(Ruling(reference = "ref1", "code", Instant.now, Instant.now, "justification", "description"))
      givenAnExistingDocument(Ruling(reference = "ref2", "code", Instant.now, Instant.now, "justification", "description"))

      await(repository.delete("ref1"))

      thenTheDocumentCountShouldBe(1)
    }
  }

  "Get by Reference" should {
    "Retrieve None" in {
      await(repository.get("some id")) shouldBe None
    }

    "Retrieve One" in {
      // Given
      val document = Ruling(reference = "ref", "code", Instant.now, Instant.now, "justification", "description")
      givenAnExistingDocument(document)

      await(repository.get("ref")) shouldBe Some(document)
    }
  }

  "Get by SimpleSearch" should {
    "Retrieve None" in {
      await(repository.get(SimpleSearch("ref", 1, 100))).results shouldBe Seq.empty
    }

    "Retrieve One - by Reference - exact match" in {
      // Given
      val document1 = Ruling(reference = "ref1", "0", Instant.now, Instant.now, "justification", "description")
      val document2 = Ruling(reference = "ref11", "0", Instant.now, Instant.now, "justification", "description")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch("ref1", 1, 100))).results shouldBe Seq(document1)
    }

    "Retrieve One - by Commodity Code - stars with" in {
      // Given
      val document1 = Ruling(reference = "ref1", "00", Instant.now, Instant.now, "justification", "description")
      val document2 = Ruling(reference = "ref2", "10", Instant.now, Instant.now, "justification", "description")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch("0", 1, 100))).results shouldBe Seq(document1)
    }

    "Retrieve One - by Goods Description - case insensitive conains" in {
      // Given
      val document1 = Ruling(reference = "ref1", "0", Instant.now, Instant.now, "justification", "fountain pen")
      val document2 = Ruling(reference = "ref2", "0", Instant.now, Instant.now, "justification", "laptop")
      givenAnExistingDocument(document1)
      givenAnExistingDocument(document2)

      await(repository.get(SimpleSearch("Tain", 1, 100))).results shouldBe Seq(document1)
    }
  }

  private def givenAnExistingDocument(ruling: Ruling): Unit = {
    await(repository.collection.insert(ruling))
  }

  private def thenTheDocumentCountShouldBe(count: Int): Unit = {
    await(repository.collection.count()) shouldBe count
  }

}
