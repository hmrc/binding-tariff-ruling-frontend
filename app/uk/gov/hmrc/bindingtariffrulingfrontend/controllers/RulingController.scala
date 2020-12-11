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

import cats.data.OptionT
import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.{AdminAction, AllowedAction, AuthenticatedAction}
import uk.gov.hmrc.bindingtariffrulingfrontend.service.{FileStoreService, RulingService}
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class RulingController @Inject() (
  rulingService: RulingService,
  fileStoreService: FileStoreService,
  allowlist: AllowedAction,
  authenticate: AuthenticatedAction,
  verifyAdmin: AdminAction,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
) extends FrontendController(mcc)
    with I18nSupport {

  type Metadata = Map[String, FileMetadata]

  def get(id: String): Action[AnyContent] = (Action andThen allowlist).async { implicit request =>
    val maybeRulingDetails = for {
      ruling       <- OptionT(rulingService.get(id))
      fileMetadata <- OptionT.liftF[Future, Metadata](fileStoreService.get(ruling))
    } yield Ok(views.html.ruling(ruling, fileMetadata))

    maybeRulingDetails.getOrElse(NotFound(views.html.ruling_not_found(id)))
  }

  def post(id: String): Action[AnyContent] = (Action andThen authenticate).async { implicit request =>
    rulingService.refresh(id).map(_ => Accepted)
  }

  def deleteAll(): Action[AnyContent] = (Action andThen verifyAdmin andThen authenticate).async {
    rulingService.deleteAll().map(_ => NoContent)
  }

  def delete(id: String): Action[AnyContent] = (Action andThen verifyAdmin andThen authenticate).async {
    rulingService.delete(id).map(_ => NoContent)
  }

}
