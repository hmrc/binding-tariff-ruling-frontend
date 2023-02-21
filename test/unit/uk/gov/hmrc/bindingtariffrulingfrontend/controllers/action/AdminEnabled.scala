/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.Mockito
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AdminEnabled extends AdminAction(Mockito.mock(classOf[AppConfig])) {
  protected override def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] =
    Future.successful(Right(request))
}

object AdminEnabled {
  def apply(): AdminEnabled = new AdminEnabled()
}
