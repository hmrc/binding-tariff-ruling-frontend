/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import uk.gov.hmrc.bindingtariffrulingfrontend.{TestMetrics, WiremockTestServer}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model._
import uk.gov.hmrc.play.audit.http.HttpAuditing

import scala.concurrent.ExecutionContext.Implicits.global

class BindingTariffClassificationConnectorSpec extends BaseSpec with WiremockTestServer {

  private val actorSystem = ActorSystem.create("testActorSystem")

  protected val appConfig: AppConfig = mock[AppConfig]

  private val wsClient: WSClient         = app.injector.instanceOf[WSClient]
  private val httpAuditing: HttpAuditing = app.injector.instanceOf[HttpAuditing]
  private val client                     = new AuthenticatedHttpClient(httpAuditing, wsClient, actorSystem, realConfig)
  private val metrics                    = new TestMetrics

  private val connector = new BindingTariffClassificationConnector(appConfig, client, metrics)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Mockito.reset(appConfig)
    given(appConfig.bindingTariffClassificationUrl).willReturn(wireMockUrl)
  }

  "Connector 'GET Case'" should {
    val startDate        = Instant.now().plus(10, ChronoUnit.SECONDS)
    val endDate          = Instant.now()
    val validDecision    = Decision("code", Some(startDate), Some(endDate), "justification", "description")
    val publicAttachment = Attachment("file-id", public = true, shouldPublishToRulings = true)
    val validCase: Case = Case(
      reference   = "ref",
      status      = CaseStatus.COMPLETED,
      application = Application(`type` = ApplicationType.BTI),
      decision    = Some(validDecision),
      attachments = Seq(publicAttachment),
      keywords    = Set("keyword")
    )

    "Get valid case" in {
      val responseJSON = Json.toJson(validCase).toString()

      stubFor(
        get(urlEqualTo("/cases/ref"))
          .willReturn(
            aResponse()
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
      stubFor(
        get(urlEqualTo("/cases/ref"))
          .willReturn(
            aResponse()
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
