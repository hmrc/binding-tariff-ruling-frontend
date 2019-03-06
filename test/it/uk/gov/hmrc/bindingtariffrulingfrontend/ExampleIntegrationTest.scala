package uk.gov.hmrc.bindingtariffrulingfrontend

import org.scalatest.BeforeAndAfterEach
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder

class ExampleIntegrationTest extends WiremockFeatureTestServer with ResourceFiles with BeforeAndAfterEach {

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .configure()
    .build()

  feature("TODO") {
    scenario("TODO") {

    }
  }

}
