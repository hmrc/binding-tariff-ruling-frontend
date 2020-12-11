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
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action.AllowedAction
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.service.{FileStoreService, RulingService}
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

@Singleton
class SearchController @Inject() (
  rulingService: RulingService,
  fileStoreService: FileStoreService,
  allowlist: AllowedAction,
  mcc: MessagesControllerComponents,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport {

  type Metadata = Map[String, FileMetadata]

  private def badRequest(form: Form[SimpleSearch])(implicit request: Request[_]): Future[Result] =
    Future.successful(BadRequest(views.html.search(form)))

  private def renderView(
    form: Form[SimpleSearch],
    rulings: Option[Paged[Ruling]] = None,
    fileMetadata: Metadata         = Map.empty
  )(implicit request: Request[_]) =
    Future.successful(Ok(views.html.search(form, rulings, fileMetadata)))

  def get(query: Option[String], imagesOnly: Boolean, page: Int): Action[AnyContent] =
    (Action andThen allowlist).async { implicit request =>
      val form = SimpleSearch.form.bindFromRequest()

      form.fold(
        badRequest,
        search =>
          search.query match {
            case None =>
              renderView(form)
            case Some(_) =>
              for {
                paged        <- rulingService.get(search)
                fileMetadata <- fileStoreService.get(paged)
                html         <- renderView(form, Some(paged), fileMetadata)
              } yield html
          }
      )
    }
}
