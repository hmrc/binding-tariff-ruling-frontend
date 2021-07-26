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

package uk.gov.hmrc.bindingtariffrulingfrontend.model

import java.time.Instant
import play.api.libs.json._
import play.api.libs.functional.syntax._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

case class Ruling(
  reference: String,
  bindingCommodityCode: String,
  effectiveStartDate: Instant,
  effectiveEndDate: Instant,
  justification: String,
  goodsDescription: String,
  keywords: Set[String]    = Set.empty,
  attachments: Seq[String] = Seq.empty,
  images: Seq[String]      = Seq.empty
) {
  lazy val bindingCommodityCodeNgrams = bindingCommodityCode
    .scanLeft("") {
      case (ngram, char) => ngram + char
    }
    .filterNot(_.isEmpty)
}

object Ruling {

  object REST {
    implicit val format: OFormat[Ruling] = Json.format[Ruling]
  }

  object Mongo {

    implicit val formatInstant: Format[Instant] = MongoJavatimeFormats.instantFormat

    implicit val rulingReads: Reads[Ruling] =
      Json.using[Json.WithDefaultValues].format[Ruling]

    implicit val rulingWrites: OWrites[Ruling] = (
      (__ \ "reference").write[String] and
        (__ \ "bindingCommodityCode").write[String] and
        (__ \ "bindingCommodityCodeNGrams").write[Seq[String]] and
        (__ \ "effectiveStartDate").write[Instant](formatInstant) and
        (__ \ "effectiveEndDate").write[Instant](formatInstant) and
        (__ \ "justification").write[String] and
        (__ \ "goodsDescription").write[String] and
        (__ \ "keywords").write[Set[String]] and
        (__ \ "attachments").write[Seq[String]] and
        (__ \ "images").write[Seq[String]]
    )(ruling =>
      (
        ruling.reference,
        ruling.bindingCommodityCode,
        ruling.bindingCommodityCodeNgrams,
        ruling.effectiveStartDate,
        ruling.effectiveEndDate,
        ruling.justification,
        ruling.goodsDescription,
        ruling.keywords,
        ruling.attachments,
        ruling.images
      )
    )

    implicit val format: OFormat[Ruling] = OFormat(
      rulingReads,
      rulingWrites
    )
  }
}
