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
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.AllowedAction
import uk.gov.hmrc.bindingtariffrulingfrontend.service.FileStoreService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.bindingtariffrulingfrontend.views

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

@Singleton
class ImageController @Inject() (
  fileStoreService: FileStoreService,
  allowlist: AllowedAction,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with Logging {

  def get(rulingReference: String, imageId: String): Action[AnyContent] = (Action andThen allowlist).async {
    implicit request =>
      val fileStoreResponse = for {
        meta     <- OptionT(fileStoreService.get(imageId))
        url      <- OptionT.fromOption[Future](meta.url)
        fileName <- OptionT.fromOption[Future](meta.fileName)
        if meta.published
      } yield Ok(views.html.image(rulingReference, url, fileName))

      fileStoreResponse.getOrElse(NotFound(views.html.image_not_found(rulingReference))).recover {
        case NonFatal(e) =>
          logger.error("Exception while calling binding-tariff-filestore", e)
          BadGateway
      }
  }
}
