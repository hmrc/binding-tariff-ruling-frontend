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

import akka.util.ByteString
import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfter
import play.api.libs.ws.WSClient
import scala.collection.immutable.ListSet
import uk.gov.hmrc.bindingtariffrulingfrontend.{TestMetrics, WiremockTestServer}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata
import java.nio.charset.StandardCharsets

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FileStoreConnectorSpec extends BaseSpec with WiremockTestServer {
  val appConfig: AppConfig = mock[AppConfig]
  val httpClient           = app.injector.instanceOf[AuthenticatedHttpClient]
  val metrics              = new TestMetrics
  val wsClient             = app.injector.instanceOf[WSClient]
  given(appConfig.maxUriLength).willReturn(2048L)
  given(appConfig.bindingTariffFileStoreUrl).willReturn(wireMockUrl)
  val connector = new FileStoreConnector(appConfig, httpClient, wsClient, metrics)

  def fromFile(path: String): String = {
    val url = getClass.getClassLoader.getResource(path)
    scala.io.Source.fromURL(url, "UTF-8").getLines().mkString
  }

  "FileStoreConnector.get" should {
    "make no request if there are no attachment IDs" in {
      await(connector.get(Set.empty[String])) shouldBe Map.empty[String, FileMetadata]
    }

    "call the filestore for a single ID" in {
      stubFor(
        get(urlEqualTo("/file/d4897c0a-b92d-4cf7-8990-f40fe158be68"))
          .willReturn(
            aResponse()
              .withBody(fromFile("filestore-response-single.json"))
          )
      )

      await(connector.get("d4897c0a-b92d-4cf7-8990-f40fe158be68")) shouldBe Some(
        FileMetadata(
          "d4897c0a-b92d-4cf7-8990-f40fe158be68",
          Some("IMG_1721.JPG"),
          Some("image/jpeg"),
          Some("http://localhost:4572/digital-tariffs-local/d4897c0a-b92d-4cf7-8990-f40fe158be68"),
          published = true
        )
      )
    }

    "call the filestore for multiple IDs" in {
      stubFor(
        get(
          urlEqualTo(
            "/file?id=006491c2-a60b-46cc-9e73-5e180d3bf1ce&id=67e8b488-d5a1-4c53-abfb-e297ed479b72&id=9a7a1787-4ec1-40d7-aa75-50cb276e3d28"
          )
        ).willReturn(
          aResponse()
            .withBody(fromFile("filestore-response.json"))
        )
      )

      await(
        connector.get(
          ListSet(
            "006491c2-a60b-46cc-9e73-5e180d3bf1ce",
            "67e8b488-d5a1-4c53-abfb-e297ed479b72",
            "9a7a1787-4ec1-40d7-aa75-50cb276e3d28"
          )
        )
      ) shouldBe (
        Map(
          "006491c2-a60b-46cc-9e73-5e180d3bf1ce" -> FileMetadata(
            "006491c2-a60b-46cc-9e73-5e180d3bf1ce",
            Some("FileUploadPDF.pdf"),
            Some("application/pdf"),
            Some("http://localhost:4572/digital-tariffs-local/006491c2-a60b-46cc-9e73-5e180d3bf1ce"),
            published = true
          ),
          "9a7a1787-4ec1-40d7-aa75-50cb276e3d28" -> FileMetadata(
            "9a7a1787-4ec1-40d7-aa75-50cb276e3d28",
            Some("FileUploadJPG.jpg"),
            Some("image/jpeg"),
            Some("http://localhost:4572/digital-tariffs-local/9a7a1787-4ec1-40d7-aa75-50cb276e3d28"),
            published = true
          )
        )
      )

    }

    "download a file from the given URL" in {
      val content = "Some content".getBytes(StandardCharsets.UTF_8)

      stubFor(
        get(urlEqualTo("/digital-tariffs-local/b4a5374f-9b47-40be-a509-cc7b349d67d5"))
          .willReturn(
            aResponse()
              .withBody(content)
          )
      )

      await(
        connector
          .downloadFile(s"$wireMockUrl/digital-tariffs-local/b4a5374f-9b47-40be-a509-cc7b349d67d5")
          .flatMap(maybeSource =>
            maybeSource.fold(Future.successful(ByteString.empty)) { source =>
              source.runFold(ByteString.empty) {
                case (bytes, nextBytes) => bytes ++ nextBytes
              }
            }
          )
      ) shouldBe ByteString(content)
    }
  }
}
