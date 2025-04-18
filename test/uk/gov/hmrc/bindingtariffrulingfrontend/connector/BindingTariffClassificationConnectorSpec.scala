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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import com.codahale.metrics.MetricRegistry
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.apache.http.HttpStatus
import org.mockito.Mockito
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.Json
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.*
import uk.gov.hmrc.bindingtariffrulingfrontend.model.{Paged, SimplePagination}
import uk.gov.hmrc.bindingtariffrulingfrontend.util.WiremockTestServer
import uk.gov.hmrc.bindingtariffrulingfrontend.utils.CaseQueueBuilder
import uk.gov.hmrc.http.client.HttpClientV2

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global

class BindingTariffClassificationConnectorSpec extends BaseSpec with WiremockTestServer with CaseQueueBuilder {

  protected val appConfig: AppConfig = mock(classOf[AppConfig])

  private val client  = app.injector.instanceOf[HttpClientV2]
  private val metrics = new MetricRegistry

  private val connector = new BindingTariffClassificationConnector(appConfig, client, metrics)
  private val xApiToken = "X-Api-Token"

  override def beforeAll(): Unit = {
    super.beforeAll()
    Mockito.reset(appConfig)
    when(appConfig.bindingTariffClassificationUrl).thenReturn(wireMockUrl)
    when(appConfig.authorization).thenReturn(xApiToken)
  }

  "Connector 'GET Case'" should {
    val startDate        = Instant.now().plus(10, ChronoUnit.SECONDS)
    val endDate          = Instant.now()
    val validDecision    = Decision("code", Some(startDate), Some(endDate), "justification", "description")
    val publicAttachment = Attachment("file-id", public = true, shouldPublishToRulings = true)
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

      stubFor(
        get(urlEqualTo("/cases/ref"))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(responseJSON)
          )
      )

      await(connector.get("ref")) shouldBe Some(validCase)

      WireMock.verify(
        getRequestedFor(urlEqualTo("/cases/ref"))
          .withHeader("X-Api-Token", equalTo(xApiToken))
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

      WireMock.verify(
        getRequestedFor(urlEqualTo("/cases/ref"))
          .withHeader("X-Api-Token", equalTo(xApiToken))
      )
    }
  }

  "get newApprovedRulings" should {

    val startDate        = LocalDate.now().atStartOfDay().minusHours(12).toInstant(ZoneOffset.UTC)
    val endDate          = LocalDate.of(2022, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    val validDecision    = Decision("code", Some(startDate), Some(endDate), "justification", "description")
    val publicAttachment = Attachment("file-id", public = true, shouldPublishToRulings = true)
    val validCase: Case = Case(
      reference = "ref",
      status = CaseStatus.COMPLETED,
      application = Application(`type` = ApplicationType.BTI),
      decision = Some(validDecision),
      attachments = Seq(publicAttachment),
      keywords = Set("keyword")
    )

    "return new cases with status Completed" in {

      val url = buildQueryUrl(
        types = Seq(ApplicationType.BTI),
        statuses = "COMPLETED",
        minDecisionStart = Some(LocalDate.now().atStartOfDay().minusHours(12).toInstant(ZoneOffset.UTC)),
        minDecisionEnd = None,
        pagination = SimplePagination()
      )

      val responseJSON = Json.toJson(Paged(Seq(validCase))).toString()

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(responseJSON)
          )
      )

      await(connector.newApprovedRulings(startDate, SimplePagination())) shouldBe Paged(Seq(validCase))

      WireMock.verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(xApiToken))
      )
    }

    "Return Paged(Empty) for 404" in {

      val url = buildQueryUrl(
        types = Seq(ApplicationType.BTI),
        statuses = "COMPLETED",
        minDecisionStart = Some(LocalDate.now().atStartOfDay().minusHours(12).toInstant(ZoneOffset.UTC)),
        minDecisionEnd = None,
        pagination = SimplePagination()
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(Json.toJson(Paged.empty[Case]).toString())
          )
      )

      await(connector.newApprovedRulings(startDate, SimplePagination())) shouldBe Paged.empty[Case]

      WireMock.verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(xApiToken))
      )
    }
  }

  "get newCanceledRulings" should {

    val startDate        = LocalDate.of(2017, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    val endDate          = LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)
    val validDecision    = Decision("code", Some(startDate), Some(endDate), "justification", "description")
    val publicAttachment = Attachment("file-id", public = true, shouldPublishToRulings = true)
    val validCase: Case = Case(
      reference = "ref",
      status = CaseStatus.CANCELLED,
      application = Application(`type` = ApplicationType.BTI),
      decision = Some(validDecision),
      attachments = Seq(publicAttachment),
      keywords = Set("keyword")
    )

    "return new cases with status CANCELLED" in {

      val url = buildQueryUrl(
        types = Seq(ApplicationType.BTI),
        statuses = "CANCELLED",
        minDecisionStart = None,
        minDecisionEnd = Some(LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)),
        pagination = SimplePagination()
      )

      val responseJSON = Json.toJson(Paged(Seq(validCase))).toString()

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(responseJSON)
          )
      )

      await(connector.newCanceledRulings(endDate, SimplePagination())) shouldBe Paged(Seq(validCase))

      WireMock.verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(xApiToken))
      )
    }

    "Return Paged(Empty) for 404" in {

      val url = buildQueryUrl(
        types = Seq(ApplicationType.BTI),
        statuses = "CANCELLED",
        minDecisionStart = None,
        minDecisionEnd = Some(LocalDate.of(2020, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC)),
        pagination = SimplePagination()
      )

      stubFor(
        get(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(HttpStatus.SC_OK)
              .withBody(Json.toJson(Paged.empty[Case]).toString())
          )
      )

      await(connector.newCanceledRulings(endDate, SimplePagination())) shouldBe Paged.empty[Case]

      WireMock.verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("X-Api-Token", equalTo(xApiToken))
      )
    }
  }
}
