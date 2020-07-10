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

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

@Singleton
class AppConfig @Inject()(
                           val runModeConfiguration: Configuration,
                           runMode: RunMode
                         ) extends ServicesConfig(runModeConfiguration, runMode) {

  lazy val assetsPrefix: String = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  lazy val analyticsToken: String = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String = loadConfig(s"google-analytics.host")
  lazy val reportAProblemPartialUrl: String = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  lazy val authorization: String = loadConfig("auth.api-token")
  lazy val bindingTariffClassificationUrl: String = baseUrl("binding-tariff-classification")
  lazy val adminEnabled: Boolean = getBoolean("admin-mode")
  lazy val whitelist: Option[Set[String]] = {
    if (getBoolean("filters.whitelist.enabled")) {
      Some[Set[String]](
        getString("filters.whitelist.ips")
          .split(",")
          .map(_.trim)
          .filter(_.nonEmpty)
          .toSet
      )
    } else None
  }
  private val contactHost = runModeConfiguration.getOptional[String](s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "BindingTariffRulings"

  private def loadConfig(key: String) = runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

}
