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

package uk.gov.hmrc.bindingtariffrulingfrontend.controllers.forms

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.Assertion
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec

class SimpleSearchSpec extends BaseSpec with ScalaCheckPropertyChecks {

  def checkForError(form: Form[_], data: Map[String, String], expectedErrors: Seq[FormError]): Assertion =
    form
      .bind(data)
      .fold(
        formWithErrors => {
          for (error <- expectedErrors)
            formWithErrors.errors      should contain(FormError(error.key, error.message, error.args))
          formWithErrors.errors.size shouldBe expectedErrors.size
        },
        _ => fail("Expected a validation error when binding the form, but it was bound successfully.")
      )

  def error(key: String, value: String, args: Any*): Seq[FormError] = Seq(FormError(key, value, args))

  lazy val emptyForm: Map[String, String] = Map[String, String]()

  def fieldThatBindsValidData(form: Form[_], fieldName: String, validDataGenerator: Gen[String]): Unit =
    "must bind valid data" in {

      forAll(validDataGenerator -> "validDataItem") { dataItem: String =>
        val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
        result.value.get shouldBe dataItem
      }
    }

  def optionalField(form: Form[_], fieldName: String): Unit = {

    "must bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors shouldEqual Seq.empty
      result.value shouldBe None
    }

    "must bind blank values" in {
      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors shouldEqual Seq.empty
      result.value.get shouldBe ""
    }
  }

  def nonEmptyString: Gen[String] =
    arbitrary[String] suchThat (_.nonEmpty)

  val form: Form[SimpleSearch] = SimpleSearch.form

  ".page" should {

    val fieldName = "page"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      nonEmptyString
    )

    behave like optionalField(
      form,
      fieldName
    )
  }

}
