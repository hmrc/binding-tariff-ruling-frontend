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

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.*
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import uk.gov.hmrc.bindingtariffrulingfrontend.filters.RateLimitFilter
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, Ruling}
import uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms.SimpleSearch
import uk.gov.hmrc.bindingtariffrulingfrontend.service.{FileStoreService, RulingService}
import uk.gov.hmrc.bindingtariffrulingfrontend.views
import uk.gov.hmrc.play.bootstrap.controller.WithUnsafeDefaultFormBinding
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SearchController @Inject() (
  rulingService: RulingService,
  fileStoreService: FileStoreService,
  rateLimit: RateLimitFilter,
  mcc: MessagesControllerComponents,
  search: views.html.search,
  implicit val appConfig: AppConfig
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc)
    with I18nSupport
    with WithUnsafeDefaultFormBinding {

  type Metadata = Map[String, FileMetadata]

  private def renderView(
    form: Form[SimpleSearch],
    rulings: Option[Paged[Ruling]],
    fileMetadata: Metadata
  )(implicit request: Request[?]) =
    Future.successful(Ok(search(form, rulings, fileMetadata)))

  def get(
    query: Option[String],
    images: Boolean,
    page: Int,
    enableTrackingConsent: Boolean = false
  ): Action[AnyContent] =
    (Action andThen rateLimit).async { implicit request =>
      val form = SimpleSearch.form.bindFromRequest()

      form.fold(
        formWithErrors => allResultsView(formWithErrors, 1),
        search =>
          for {
            paged        <- rulingService.get(search)
            fileMetadata <- fileStoreService.get(paged)
            html         <- renderView(form, Some(paged), fileMetadata)
          } yield html
      )
    }

  private def allResultsView(form: Form[SimpleSearch], page: Int)(implicit request: Request[?]): Future[Result] = {
    val search = SimpleSearch(query = None, imagesOnly = false, pageIndex = page)

    for {
      paged        <- rulingService.get(search)
      fileMetadata <- fileStoreService.get(paged)
      html         <- renderView(form, Some(paged), fileMetadata)
    } yield html
  }
}
