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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.connector.model.FileMetadata

class FileMetadataSpec extends BaseSpec {
  "FileMetadata" should {
    "serialize and deserialize using JSON formatter" in {
      val metadata = FileMetadata(
        id = "test-id",
        fileName = Some("test-file.png"),
        mimeType = Some("image/png"),
        url = Some("http://example.com/test-file.png"),
        published = true
      )

      val formatter = FileMetadata.outboundFormat

      val json = formatter.writes(metadata)

      (json \ "id").as[String]         shouldBe "test-id"
      (json \ "fileName").as[String]   shouldBe "test-file.png" // Note the capital N
      (json \ "mimeType").as[String]   shouldBe "image/png"
      (json \ "url").as[String]        shouldBe "http://example.com/test-file.png"
      (json \ "published").as[Boolean] shouldBe true

      val metadataFromJson = formatter.reads(json).get

      metadataFromJson shouldBe metadata

      metadata.isImage shouldBe true

      FileMetadata("id", Some("file.jpg"), Some("image/jpeg"), None, false).isImage shouldBe true
      FileMetadata("id", Some("file.gif"), Some("image/gif"), None, false).isImage  shouldBe true
      FileMetadata("id", Some("file.txt"), Some("text/plain"), None, false).isImage shouldBe false
      FileMetadata("id", Some("file.txt"), None, None, false).isImage               shouldBe false
    }
  }
}
