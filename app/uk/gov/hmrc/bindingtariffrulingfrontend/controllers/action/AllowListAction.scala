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
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.filters.AllowListFilter
import play.api.mvc.{ActionFunction, Request, Result}

import scala.concurrent.{ExecutionContext, Future}

class AllowListAction @Inject() (
  appConfig: AppConfig,
  val allowList: AllowListFilter
)(implicit val executionContext: ExecutionContext)
    extends ActionFunction[Request, Request] {
  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    if (appConfig.allowListEnabled) allowList(_ => block(request))(request) else block(request)
}
