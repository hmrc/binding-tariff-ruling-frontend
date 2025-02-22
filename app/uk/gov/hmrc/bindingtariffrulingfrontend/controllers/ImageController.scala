/*
 * Copyright 2025 HM Revenue & Customs
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
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.service.FileStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.bindingtariffrulingfrontend.views

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class ImageController @Inject() (
  fileStoreService: FileStoreService,
  mcc: MessagesControllerComponents,
  imageView: views.html.image,
  notFoundView: views.html.not_found,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def get(rulingReference: String, imageId: String): Action[AnyContent] = Action.async { implicit request =>
    if (appConfig.displayImages) {
      val fileStoreResponse = for {
        meta     <- OptionT(fileStoreService.get(imageId))
        fileName <- OptionT.fromOption[Future](meta.fileName)
      } yield Ok(imageView(rulingReference, imageId, fileName))

      fileStoreResponse.getOrElse(NotFound(notFoundView())).recover { case NonFatal(e) =>
        logger.error("[ImageController][get]Exception while calling binding-tariff-filestore", e)
        BadGateway
      }
    } else {
      Future.successful(Redirect(controllers.routes.Default.redirect()))
    }
  }
}
