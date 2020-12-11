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

import org.mockito.Mockito._
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.FileStoreConnector
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Paged
import uk.gov.hmrc.bindingtariffrulingfrontend.model.Ruling
import java.time.Instant
import org.scalatest.BeforeAndAfterEach

class FileStoreServiceTest extends BaseSpec with BeforeAndAfterEach {
  val connector = mock[FileStoreConnector]
  val service   = new FileStoreService(connector)

  def makeRuling(id: Int = 0) = Ruling(
    "ref" + id,
    "10101010",
    Instant.now,
    Instant.now,
    "justification",
    "description",
    Set("foo", "bar"),
    Seq("file1", "file2"),
    Seq("image1", "image2")
  )

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(connector)
  }

  "FileStoreService.get with single id" should {
    "delegate to connector" in {
      val id = "id"
      service.get(id)
      verify(connector).get(id)
    }
  }

  "FileStoreService.get with ids" should {
    "delegate to connector" in {
      val ids = Seq("id1", "id2")
      service.get(ids)
      verify(connector).get(ids.toSet)
    }
  }

  "FileStoreService.get for a single ruling" should {
    val ruling = makeRuling()

    "delegate to connector" in {
      service.get(ruling)
      verify(connector).get(Set("file1", "file2", "image1", "image2"))
    }
  }

  "FileStoreService.get with page of rulings" should {
    "delegate to connector" in {
      val rulings = Paged((0 until 5).map(makeRuling))
      service.get(rulings)
      val ids = Seq("file1", "file2", "image1", "image2")
      verify(connector).get(Seq.fill(5)(ids).flatten.toSet)
    }
  }
}
