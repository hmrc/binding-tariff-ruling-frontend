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

import javax.inject.Inject
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.repository.RulingRepository

import scala.concurrent.Future

class RulingService @Inject()(repository: RulingRepository) {

  def get(id: String): Future[Option[Ruling]] = {
    repository.get(id)
  }

  def get(query: SimpleSearch): Future[Paged[Ruling]] = {
    repository.get(query)
  }

  def refresh(id: String): Future[Unit] = {
    Future.successful(())
  }

}
