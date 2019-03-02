package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action

import org.mockito.Mockito
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig

import scala.concurrent.Future

class SuccessfulAuth extends AuthenticatedAction(Mockito.mock(classOf[AppConfig])) {
  protected override def refine[A](request: Request[A]): Future[Either[Result, Request[A]]] = Future.successful(Right(request))
}

object SuccessfulAuth {
  def apply(): SuccessfulAuth = new SuccessfulAuth()
}
