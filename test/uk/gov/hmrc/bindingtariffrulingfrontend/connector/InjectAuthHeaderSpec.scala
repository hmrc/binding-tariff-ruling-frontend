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

package uk.gov.hmrc.bindingtariffrulingfrontend.connector

import org.mockito.Mockito.{mock, verify, verifyNoInteractions, when}
import uk.gov.hmrc.bindingtariffrulingfrontend.base.BaseSpec
import uk.gov.hmrc.bindingtariffrulingfrontend.config.AppConfig
import uk.gov.hmrc.http.HeaderCarrier

class InjectAuthHeaderSpec extends BaseSpec {

  // Create a class that extends the trait for testing
  class TestInjectAuthHeader extends InjectAuthHeader

  "InjectAuthHeader" should {
    "use existing header when found by headers method" in {
      // Setup
      val testClass  = new TestInjectAuthHeader()
      val mockConfig = mock(classOf[AppConfig])

      // Create a mock HeaderCarrier instead of a real one
      val mockHeaderCarrier = mock(classOf[HeaderCarrier])

      // Set up the mock to return what we want for the headers method
      when(mockHeaderCarrier.headers(Seq("X-Api-Token")))
        .thenReturn(Seq("X-Api-Token" -> "existing-token"))

      // Use the mocked HeaderCarrier
      implicit val hc = mockHeaderCarrier

      // Execute
      val result = testClass.authHeaders(mockConfig)

      // Verify
      result shouldBe Seq("X-Api-Token" -> "existing-token")
      verifyNoInteractions(mockConfig)
    }

    "use config authorization when header is not present" in {
      // Setup
      val testClass   = new TestInjectAuthHeader()
      val mockConfig  = mock(classOf[AppConfig])
      val configToken = "config-token"
      when(mockConfig.authorization).thenReturn(configToken)

      // Create a mock HeaderCarrier
      val mockHeaderCarrier = mock(classOf[HeaderCarrier])
      when(mockHeaderCarrier.headers(Seq("X-Api-Token"))).thenReturn(Seq.empty)
      implicit val hc = mockHeaderCarrier

      // Execute
      val result = testClass.authHeaders(mockConfig)

      // Verify
      result shouldBe Seq("X-Api-Token" -> configToken)
      verify(mockConfig).authorization
    }
  }
}
