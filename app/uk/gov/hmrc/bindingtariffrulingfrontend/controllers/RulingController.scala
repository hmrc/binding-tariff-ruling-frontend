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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.{AdminAction, AuthenticatedAction, AllowedAction}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.RulingService
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RulingController @Inject()(
                                  rulingService: RulingService,
                                  allowlist: AllowedAction,
                                  authenticate: AuthenticatedAction,
                                  verifyAdmin: AdminAction,
                                  mcc: MessagesControllerComponents,
                                  implicit val appConfig: AppConfig
                                ) extends FrontendController(mcc) with I18nSupport {

  def get(id: String): Action[AnyContent] = (Action andThen allowlist).async { implicit request =>
    rulingService.get(id) map {
      case Some(ruling) => Ok(views.html.ruling(ruling))
      case _ => Ok(views.html.ruling_not_found(id))
    }
  }

  def post(id: String): Action[AnyContent] = (Action andThen authenticate).async { implicit request =>
    rulingService.refresh(id).map(_ => Accepted)
  }

  def delete(): Action[AnyContent] = (Action andThen verifyAdmin andThen authenticate).async {
    rulingService.delete().map(_ => NoContent)
  }

}
