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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import java.time.Instant
import java.time.temporal.ChronoUnit

import akka.actor.ActorSystem
import com.github.tomakehurst.wiremock.client.WireMock._
import org.apache.http.HttpStatus
import org.mockito.BDDMockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.bindingtariffadvicefrontend.WiremockTestServer
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.{Attachment, Case, CaseStatus, Decision}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.bootstrap.http.DefaultHttpClient
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}


class BindingTariffClassificationConnectorSpec extends UnitSpec
  with WiremockTestServer with MockitoSugar with WithFakeApplication {

  private val configuration = mock[AppConfig]
  private val actorSystem = ActorSystem.create("test")
  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new DefaultHttpClient(fakeApplication.configuration, auditConnector, wsClient, actorSystem)
  private implicit val hc = HeaderCarrier()

  private val connector = new BindingTariffClassificationConnector(configuration, client)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    given(configuration.bindingTariffClassificationUrl).willReturn(wireMockUrl)
  }

  "Connector 'GET Case'" should {
    val startDate = Instant.now().plus(10, ChronoUnit.SECONDS)
    val endDate = Instant.now()
    val validDecision = Decision("code", Some(startDate), Some(endDate), "justification", "description")
    val publicAttachment = Attachment("file-id", public = true)
    val validCase: Case = Case(
      reference = "ref",
      status = CaseStatus.COMPLETED,
      decision = Some(validDecision),
      attachments = Seq(publicAttachment),
      keywords = Set("keyword")
    )

    "Get valid case" in {
      val responseJSON = Json.toJson(validCase).toString()

      stubFor(get(urlEqualTo(s"/cases/ref"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(responseJSON)
        )
      )

      await(connector.get("ref")) shouldBe Some(validCase)
    }

    "Return None for 404" in {
      stubFor(get(urlEqualTo(s"/cases/ref"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND)
        )
      )

      await(connector.get("ref")) shouldBe None
    }
  }

}
