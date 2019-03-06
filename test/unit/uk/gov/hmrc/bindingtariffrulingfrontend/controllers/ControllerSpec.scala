/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.LOCATION
import play.api.test.{FakeHeaders, FakeRequest}
import play.filters.csrf.CSRF.{Token, TokenProvider}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

trait ControllerSpec extends UnitSpec with GuiceOneAppPerSuite with MockitoSugar {

  protected def locationOf(result: Result): Option[String] = {
    result.header.headers.get(LOCATION)
  }

  protected def getRequestWithCSRF(uri: String = "/"): FakeRequest[AnyContentAsEmpty.type] = {
    val tokenProvider: TokenProvider = fakeApplication().injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest("GET", uri, FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  }

  protected def deleteRequestWithCSRF: FakeRequest[AnyContentAsEmpty.type] = {
    val tokenProvider: TokenProvider = fakeApplication().injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest("DELETE", "/", FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  }

  protected def postRequestWithCSRF: FakeRequest[AnyContentAsEmpty.type] = {
    val tokenProvider: TokenProvider = fakeApplication().injector.instanceOf[TokenProvider]
    val csrfTags = Map(Token.NameRequestTag -> "csrfToken", Token.RequestTag -> tokenProvider.generateToken)
    FakeRequest("POST", "/", FakeHeaders(), AnyContentAsEmpty, tags = csrfTags)
  }

  protected def returnTheFirstArgument[T]: Answer[Future[T]] = new Answer[Future[T]] {
    override def answer(invocation: InvocationOnMock): Future[T] = Future.successful(invocation.getArgument(0))
  }

}
