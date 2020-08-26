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

import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.Future.successful
import scala.concurrent.ExecutionContext.Implicits.global

class AllowedAction @Inject()(appConfig: AppConfig) extends ActionRefiner[Request, Request] {

  override protected def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = {
    appConfig.allowlist match {
      case Some(addresses: Set[String]) =>
        request.headers.get("True-Client-IP") match {
          case Some(ip: String) if addresses.contains(ip) => successful(Right(request))
          case _ => successful(Left(Results.Forbidden))
        }
      case _ => successful(Right(request))
    }
  }

  override protected def executionContext: ExecutionContext = global
}
