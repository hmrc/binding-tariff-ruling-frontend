package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action

import org.mockito.Mockito
import play.api.mvc.{Request, Result, Results}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

import scala.concurrent.Future

class FailedAuth extends AuthenticatedAction(Mockito.mock(classOf[AppConfig])) {
  protected override def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = Future.successful(Left(Results.Forbidden))
}

object FailedAuth {
  def apply(): FailedAuth = new FailedAuth()
}


