package uk.gov.hmrc.bindingtariffrulingfrontend

import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.bindingtariffadvicefrontend.{ResourceFiles, WiremockFeatureTestServer}

class ExampleIntegrationTest extends WiremockFeatureTestServer with ResourceFiles with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    super.beforeEach()
  }

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  feature("") {
    scenario("") {

    }
  }
}
