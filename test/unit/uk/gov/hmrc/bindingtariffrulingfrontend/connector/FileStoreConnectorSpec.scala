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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import com.github.tomakehurst.wiremock.client.WireMock._
import org.mockito.BDDMockito._
import org.scalatest.BeforeAndAfter
import scala.collection.immutable.ListSet
import scala.io.Source
import uk.gov.hmrc.bindingtariffrulingfrontend.WiremockTestServer
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata

class FileStoreConnectorSpec extends BaseSpec with WiremockTestServer with BeforeAndAfter {
  val appConfig: AppConfig = mock[AppConfig]
  val httpClient           = app.injector.instanceOf[AuthenticatedHttpClient]
  given(appConfig.maxUriLength).willReturn(2048L)
  val connector = new FileStoreConnector(appConfig, httpClient)

  override def beforeAll(): Unit = {
    super.beforeAll()
    given(appConfig.bindingTariffFileStoreUrl).willReturn(wireMockUrl)
  }

  def fromFile(path: String): String = {
    val url = getClass.getClassLoader.getResource(path)
    Source.fromURL(url, "UTF-8").getLines().mkString
  }

  "FileStoreConnector.get" should {
    "make no request if there are no attachment IDs" in {
      await(connector.get(Set.empty[String])) shouldBe Map.empty[String, FileMetadata]
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
          "67e8b488-d5a1-4c53-abfb-e297ed479b72" -> FileMetadata(
            "67e8b488-d5a1-4c53-abfb-e297ed479b72",
            Some("my-file.txt"),
            Some("application/binary"),
            Some("http://localhost:9570/upscan/download/3931e8db-7b6f-42cc-8520-e9812643e8a1"),
            published = false
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
  }
}
