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

package uk.gov.hmrc.bindingtariffrulingfrontend.base

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Lang, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.util.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier

import scala.io.Source.fromURL

trait BaseSpec extends UnitSpec with GuiceOneAppPerSuite {

  override implicit lazy val app: Application = GuiceApplicationBuilder()
    .configure(
      "metrics.jvm"     -> false,
      "metrics.enabled" -> false
    )
    .build()

  lazy val realConfig: AppConfig                      = app.injector.instanceOf[AppConfig]
  lazy val messageApi: MessagesApi                    = app.injector.instanceOf[MessagesApi]
  implicit lazy val mcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  implicit val hc: HeaderCarrier                      = HeaderCarrier()
  implicit lazy val mat: Materializer                 = app.materializer
  implicit lazy val ac: ActorSystem                   = app.actorSystem
  implicit lazy val lang: Lang                        = mcc.langs.availables.head

  def fromFile(path: String): String = {
    val url    = getClass.getClassLoader.getResource(path)
    val source = fromURL(url, "UTF-8")
    try source.getLines().mkString
    finally source.close()
  }

}
