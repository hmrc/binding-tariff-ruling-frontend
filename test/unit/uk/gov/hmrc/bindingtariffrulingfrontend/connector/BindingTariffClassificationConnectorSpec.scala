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
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import play.api.Environment
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.bindingtariffrulingfrontend.WiremockTestServer
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.audit.DefaultAuditConnector
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class BindingTariffClassificationConnectorSpec extends UnitSpec
  with WiremockTestServer with MockitoSugar with WithFakeApplication {

  private val actorSystem = ActorSystem.create("testActorSystem")

  protected implicit val realConfig: AppConfig = fakeApplication.injector.instanceOf[AppConfig]
  protected val appConfig: AppConfig = mock[AppConfig]

  private val wsClient: WSClient = fakeApplication.injector.instanceOf[WSClient]
  private val auditConnector = new DefaultAuditConnector(fakeApplication.configuration, fakeApplication.injector.instanceOf[Environment])
  private val client = new AuthenticatedHttpClient(auditConnector, wsClient, actorSystem)
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val connector = new BindingTariffClassificationConnector(appConfig, client)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Mockito.reset(appConfig)
    given(appConfig.bindingTariffClassificationUrl).willReturn(wireMockUrl)
  }

  "Connector 'GET Case'" should {
    val startDate = Instant.now().plus(10, ChronoUnit.SECONDS)
    val endDate = Instant.now()
    val validDecision = Decision("code", Some(startDate), Some(endDate), "justification", "description")
    val publicAttachment = Attachment("file-id", public = true)
    val validCase: Case = Case(
      reference = "ref",
      status = CaseStatus.COMPLETED,
      application = Application(`type` = ApplicationType.BTI),
      decision = Some(validDecision),
      attachments = Seq(publicAttachment),
      keywords = Set("keyword")
    )

    "Get valid case" in {
      val responseJSON = Json.toJson(validCase).toString()

      stubFor(get(urlEqualTo("/cases/ref"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_OK)
          .withBody(responseJSON)
        )
      )

      await(connector.get("ref")) shouldBe Some(validCase)

      verify(
        getRequestedFor(urlEqualTo("/cases/ref"))
          .withHeader("X-Api-Token", equalTo(realConfig.authorization))
      )
    }

    "Return None for 404" in {
      stubFor(get(urlEqualTo("/cases/ref"))
        .willReturn(aResponse()
          .withStatus(HttpStatus.SC_NOT_FOUND)
        )
      )

      await(connector.get("ref")) shouldBe None

      verify(
        getRequestedFor(urlEqualTo("/cases/ref"))
          .withHeader("X-Api-Token", equalTo(realConfig.authorization))
      )
    }
  }

}
