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
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject() (val configuration: Configuration) extends ServicesConfig(configuration) {

  private lazy val contactHost             = configuration.getOptional[String](s"contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "AdvanceTariffRulings"

  lazy val assetsPrefix: String                   = loadConfig[String](s"assets.url") + loadConfig[String](s"assets.version")
  lazy val analyticsToken: String                 = loadConfig[String](s"google-analytics.token")
  lazy val analyticsHost: String                  = loadConfig[String](s"google-analytics.host")
  lazy val authorization: String                  = loadConfig[String]("auth.api-token")
  lazy val bindingTariffClassificationUrl: String = baseUrl("binding-tariff-classification")
  lazy val bindingTariffFileStoreUrl: String      = baseUrl("binding-tariff-filestore")
  lazy val adminEnabled: Boolean                  = getBoolean("admin-mode")
  lazy val ukGlobalTariffHost: String             = loadConfig[String]("uk-global-tariff.host")

  lazy val maxUriLength: Long = configuration.underlying.getBytes("akka.http.parsing.max-uri-length")

  lazy val allowListEnabled     = loadConfig[Boolean]("filters.allowlist.enabled")
  lazy val allowListDestination = loadConfig[String]("filters.allowlist.destination")
  lazy val allowList: Set[String] =
    loadConfig[String]("filters.allowlist.ips")
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
      .toSet

  lazy val reportAProblemPartialUrl: String =
    s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String =
    s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val betaFeedbackUrl = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"
  lazy val betaFeedbackUnauthenticatedUrl =
    s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  lazy val rateLimitBucketSize: Int    = loadConfig[Int]("filters.rateLimit.bucketSize")
  lazy val rateLimitRatePerSecond: Int = loadConfig[Int]("filters.rateLimit.ratePerSecond")
  lazy val rateLimiterEnabled: Boolean = loadConfig[Boolean]("filters.rateLimit.enabled")

  lazy val accessibilityBaseUrl: String = loadConfig[String](s"accessibility-statement.baseUrl")
  lazy private val accessibilityRedirectUrl: String = loadConfig[String](s"accessibility-statement.redirectUrl")
  def accessibilityStatementUrl(referrer: String) =
    s"$accessibilityBaseUrl/accessibility-statement$accessibilityRedirectUrl?referrerUrl=${SafeRedirectUrl(
      accessibilityBaseUrl + referrer).encodedUrl}"

  private def loadConfig[A](key: String)(implicit loader: ConfigLoader[A]) =
    configuration.getOptional[A](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
}
