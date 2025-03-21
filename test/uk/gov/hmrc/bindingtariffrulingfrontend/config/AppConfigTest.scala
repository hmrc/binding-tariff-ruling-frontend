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

package uk.gov.hmrc.bindingtariffrulingfrontend.config

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Configuration
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class AppConfigTest extends BaseSpec {

  private def appConfig(pairs: (String, String)*): AppConfig = {
    val config = Configuration.from(pairs.map(e => e._1 -> e._2).toMap)
    new AppConfig(config)
  }

  "load configuration values correctly" in {
    // Create mock configuration
    val configuration = mock[Configuration]

    // Set up mock to return specific values for different config paths
    when(configuration.getOptional[String]("contact-frontend.host")).thenReturn(Some("test-host"))
    when(configuration.getOptional[String]("assets.url")).thenReturn(Some("test-assets-url"))
    when(configuration.getOptional[String]("assets.version")).thenReturn(Some("test-version"))
    when(configuration.getOptional[String]("auth.api-token")).thenReturn(Some("test-token"))
    when(configuration.getOptional[Boolean]("admin-mode")).thenReturn(Some(true))
    when(configuration.getOptional[String]("uk-global-tariff.host")).thenReturn(Some("test-ugt-host"))
    when(configuration.getOptional[String]("urls.helpMakeGovUkBetterUrl")).thenReturn(Some("test-help-url"))
    when(configuration.getOptional[Boolean]("toggle.displayResearchBanner")).thenReturn(Some(true))
    when(configuration.getOptional[Boolean]("toggle.displayImages")).thenReturn(Some(false))
    when(configuration.getOptional[Int]("filters.rateLimit.bucketSize")).thenReturn(Some(10))
    when(configuration.getOptional[Int]("filters.rateLimit.ratePerSecond")).thenReturn(Some(5))
    when(configuration.getOptional[Boolean]("filters.rateLimit.enabled")).thenReturn(Some(true))

    // Create AppConfig with mock configuration
    val config = new AppConfig(configuration)

    // Verify the values were loaded correctly
    config.assetsPrefix              shouldBe "test-assets-urltest-version"
    config.authorization             shouldBe "test-token"
    config.adminEnabled              shouldBe true
    config.ukGlobalTariffHost        shouldBe "test-ugt-host"
    config.helpMakeGovUkBetterUrl    shouldBe "test-help-url"
    config.displayMakeGovUkBetterUrl shouldBe true
    config.displayImages             shouldBe false
    config.reportAProblemPartialUrl  shouldBe "test-host/contact/problem_reports_ajax?service=AdvanceTariffRulings"
    config.reportAProblemNonJSUrl    shouldBe "test-host/contact/problem_reports_nonjs?service=AdvanceTariffRulings"
    config.rateLimitBucketSize       shouldBe 10
    config.rateLimitRatePerSecond    shouldBe 5
    config.rateLimiterEnabled        shouldBe true
  }

  "Build assets prefix" in {
    appConfig(
      "assets.url"     -> "http://localhost:9032/assets/",
      "assets.version" -> "4.5.0"
    ).assetsPrefix shouldBe "http://localhost:9032/assets/4.5.0"
  }

  "Build report url" in {
    appConfig(
      "contact-frontend.host" -> "host"
    ).reportAProblemPartialUrl shouldBe "host/contact/problem_reports_ajax?service=AdvanceTariffRulings"
  }

  "Build report non-json url" in {
    appConfig(
      "contact-frontend.host" -> "host"
    ).reportAProblemNonJSUrl shouldBe "host/contact/problem_reports_nonjs?service=AdvanceTariffRulings"
  }

  "Build admin enabled" in {
    appConfig("admin-mode" -> "false").adminEnabled shouldBe false
    appConfig("admin-mode" -> "true").adminEnabled  shouldBe true
  }

  "Build Classification Backend URL" in {
    appConfig(
      "microservice.services.binding-tariff-classification.port"     -> "8080",
      "microservice.services.binding-tariff-classification.host"     -> "localhost",
      "microservice.services.binding-tariff-classification.protocol" -> "http"
    ).bindingTariffClassificationUrl shouldBe "http://localhost:8080"
  }

  "throw exception if no such config" in {
    intercept[Exception](
      appConfig(
        "microservice.services.binding-tariff-classification.port" -> "8080"
      ).ukGlobalTariffHost
    ).getMessage shouldBe s"Missing configuration key: uk-global-tariff.host"
  }

}
