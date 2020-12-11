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

import org.mockito.BDDMockito._
import org.mockito.Mockito._
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.DefaultDB
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.mongo.MongoConnector

class MongoDbSpec extends BaseSpec {
  "MongoDb" should {
    "delegate to connector" in {
      val connector = mock[MongoConnector]
      val component = mock[ReactiveMongoComponent]

      given(component.mongoConnector).willReturn(connector)
      given(connector.db).willReturn(() => mock[DefaultDB])

      val mongodb = new MongoDb(component)

      verify(connector).db
    }
  }
}