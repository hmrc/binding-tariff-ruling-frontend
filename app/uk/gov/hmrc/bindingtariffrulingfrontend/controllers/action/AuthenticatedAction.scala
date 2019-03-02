package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action

import javax.inject.Inject
import play.api.mvc._
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

import scala.concurrent.Future

class AuthenticatedAction @Inject()(appConfig: AppConfig) extends ActionRefiner[Request, Request] {

  override protected def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = {
    if(request.headers.get("Authorization").contains(appConfig.authorization)) {
      Future.successful(Right(request))
    } else {
      Future.successful(Left(Results.Forbidden))
    }
  }

}