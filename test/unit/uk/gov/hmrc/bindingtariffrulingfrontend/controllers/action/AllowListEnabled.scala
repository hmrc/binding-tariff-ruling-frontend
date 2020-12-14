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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import org.mockito.Mockito
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.filters.AllowListFilter

import scala.concurrent.{ExecutionContext, Future}

class AllowListEnabled(appConfig: AppConfig)(implicit mat: Materializer, ec: ExecutionContext)
    extends AllowListAction(appConfig, new AllowListFilter(appConfig)) {
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    Future.successful(allowList.response)
}

object AllowListEnabled {
  val system       = ActorSystem.create("testActorSystem")
  val materializer = ActorMaterializer.create(system)
  def apply(): AllowListEnabled =
    new AllowListEnabled(Mockito.mock(classOf[AppConfig]))(materializer, materializer.executionContext)
}
