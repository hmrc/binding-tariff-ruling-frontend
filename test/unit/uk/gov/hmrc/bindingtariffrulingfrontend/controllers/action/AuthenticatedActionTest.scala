package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.action

import org.mockito.ArgumentMatchers._
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{Request, Result, Results}
import play.api.test.FakeRequest
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class AuthenticatedActionTest extends UnitSpec with MockitoSugar {

  private val block = mock[Request[_] => Future[Result]]
  private val config = mock[AppConfig]
  private val action = new AuthenticatedAction(config)

  "Authenticated Action" should {
    "Filter unauthenticated" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.authorization) willReturn "password"

      await(action.invokeBlock(FakeRequest(), block)) shouldBe Results.Forbidden
    }

    "Filter authenticated" in {
      given(block.apply(any[Request[_]])) willReturn Future.successful(Results.Ok)
      given(config.authorization) willReturn "password"

      await(action.invokeBlock(FakeRequest().withHeaders("Authorization" -> "password"), block)) shouldBe Results.Ok
    }
  }

}
