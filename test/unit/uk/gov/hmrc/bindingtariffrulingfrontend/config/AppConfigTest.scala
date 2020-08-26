/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.Configuration
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class AppConfigTest extends BaseSpec {

  private def appConfig(pairs: (String, String)*): AppConfig = {
    val config = Configuration.from(pairs.map(e => e._1 -> e._2).toMap)
    new AppConfig(config)
  }

  "Build assets prefix" in {
    appConfig(
      "assets.url" -> "http://localhost:9032/assets/",
      "assets.version" -> "4.5.0"
    ).assetsPrefix shouldBe "http://localhost:9032/assets/4.5.0"
  }

  "Build analytics token" in {
    appConfig("google-analytics.token" -> "N/A").analyticsToken shouldBe "N/A"
  }

  "Build analytics host" in {
    appConfig("google-analytics.host" -> "auto").analyticsHost shouldBe "auto"
  }

  "Build report url" in {
    appConfig("contact-frontend.host" -> "host").reportAProblemPartialUrl shouldBe "host/contact/problem_reports_ajax?service=BindingTariffRulings"
  }

  "Build report non-json url" in {
    appConfig("contact-frontend.host" -> "host").reportAProblemNonJSUrl shouldBe "host/contact/problem_reports_nonjs?service=BindingTariffRulings"
  }

  "Build admin enabled" in {
    appConfig("admin-mode" -> "false").adminEnabled shouldBe false
    appConfig("admin-mode" -> "true").adminEnabled shouldBe true
  }

  "Build allowlist" in {
    appConfig(
      "filters.allowlist.enabled" -> "true",
      "filters.allowlist.ips" -> "ip1, ip2"
    ).allowlist shouldBe Some(Set("ip1", "ip2"))

    appConfig("filters.allowlist.enabled" -> "false").allowlist shouldBe None
  }

  "Build Classification Backend URL" in {
    appConfig(
      "microservice.services.binding-tariff-classification.port" -> "8080",
      "microservice.services.binding-tariff-classification.host" -> "localhost",
      "microservice.services.binding-tariff-classification.protocol" -> "http"
    ).bindingTariffClassificationUrl shouldBe "http://localhost:8080"
  }

}
