package uk.gov.hmrc.bindingtariffrulingfrontend.utils

import java.time.{LocalDate, ZoneOffset}

import uk.gov.hmrc.play.test.UnitSpec

class DatesSpec extends UnitSpec {

  "Format" should {

    "convert instant to string" in {
      val date = LocalDate.of(2018,1,1).atStartOfDay(ZoneOffset.UTC).toInstant
      val output = Dates.format(date)

      output shouldBe "01 Jan 2018"
    }

  }

}
