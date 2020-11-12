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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (val configuration: Configuration) extends ServicesConfig(configuration) {

  private lazy val contactHost                  = configuration.getOptional[String](s"contact-frontend.host").getOrElse("")
  private lazy val contactFormServiceIdentifier = configuration.get[String]("appName")

  lazy val assetsPrefix: String                   = loadConfig(s"assets.url") + loadConfig(s"assets.version")
  lazy val analyticsToken: String                 = loadConfig(s"google-analytics.token")
  lazy val analyticsHost: String                  = loadConfig(s"google-analytics.host")
  lazy val authorization: String                  = loadConfig("auth.api-token")
  lazy val bindingTariffClassificationUrl: String = baseUrl("binding-tariff-classification")
  lazy val adminEnabled: Boolean                  = getBoolean("admin-mode")

  lazy val allowlist: Option[Set[String]] = {
    if (getBoolean("filters.allowlist.enabled")) {
      Some[Set[String]](
        getString("filters.allowlist.ips")
          .split(",")
          .map(_.trim)
          .filter(_.nonEmpty)
          .toSet
      )
    } else None
  }

  lazy val reportAProblemPartialUrl: String =
    s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String =
    s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUnauthenticatedUrl =
    s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  private def loadConfig(key: String) =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

}
