/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (val configuration: Configuration) extends ServicesConfig(configuration) {

  private lazy val contactHost             = configuration.getOptional[String]("contact-frontend.host").getOrElse("")
  private val contactFormServiceIdentifier = "AdvanceTariffRulings"

  lazy val assetsPrefix: String                   = loadConfig[String]("assets.url") + loadConfig[String]("assets.version")
  lazy val authorization: String                  = loadConfig[String]("auth.api-token")
  lazy val bindingTariffClassificationUrl: String = baseUrl("binding-tariff-classification")
  lazy val bindingTariffFileStoreUrl: String      = baseUrl("binding-tariff-filestore")
  lazy val adminEnabled: Boolean                  = getBoolean("admin-mode")
  lazy val ukGlobalTariffHost: String             = loadConfig[String]("uk-global-tariff.host")
  lazy val helpMakeGovUkBetterUrl: String         = loadConfig[String]("urls.helpMakeGovUkBetterUrl")
  lazy val displayMakeGovUkBetterUrl: Boolean     = loadConfig[Boolean]("toggle.displayResearchBanner")
  lazy val displayImages: Boolean                 = loadConfig[Boolean]("toggle.displayImages")

  lazy val maxUriLength: Long = configuration.underlying.getBytes("akka.http.parsing.max-uri-length")

  lazy val reportAProblemPartialUrl: String =
    s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  lazy val reportAProblemNonJSUrl: String =
    s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  lazy val rateLimitBucketSize: Int    = loadConfig[Int]("filters.rateLimit.bucketSize")
  lazy val rateLimitRatePerSecond: Int = loadConfig[Int]("filters.rateLimit.ratePerSecond")
  lazy val rateLimiterEnabled: Boolean = loadConfig[Boolean]("filters.rateLimit.enabled")

  private def loadConfig[A](key: String)(implicit loader: ConfigLoader[A]) =
    configuration.getOptional[A](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
}
