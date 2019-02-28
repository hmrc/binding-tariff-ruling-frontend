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

package uk.gov.hmrc.bindingtariffrulingfrontend.service

import org.mockito.BDDMockito.given
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.RulingRepository
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class RulingServiceTest extends UnitSpec with MockitoSugar {

  private val repository = mock[RulingRepository]

  private val service = new RulingService(repository)

  "Service GET by reference" should {

    "delegate to repository" in {
      given(repository.get("id")) willReturn Future.successful(None)
      await(service.get("id")) shouldBe None
    }
  }

  "Service GET by search" should {
    val search = SimpleSearch("query", 1, 1)

    "delegate to repository" in {
      given(repository.get(search)) willReturn Future.successful(Paged.empty[Ruling])
      await(service.get(search)) shouldBe Paged.empty[Ruling]
    }
  }

}
