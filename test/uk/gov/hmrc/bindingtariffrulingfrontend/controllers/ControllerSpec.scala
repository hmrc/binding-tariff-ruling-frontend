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

import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.CSRFTokenHelper.CSRFFRequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.LOCATION
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

import scala.concurrent.Future

trait ControllerSpec extends BaseSpec {

  protected def locationOf(result: Result): Option[String] =
    result.header.headers.get(LOCATION)

  protected def getRequestWithCSRF(uri: String = "/"): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("GET", uri).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  protected def deleteRequestWithCSRF(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("DELETE", "/").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  protected def postRequestWithCSRF: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest("POST", "/").withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  protected def returnTheFirstArgument[T]: Answer[Future[T]] =
    (invocation: InvocationOnMock) => Future.successful(invocation.getArgument(0))

}
