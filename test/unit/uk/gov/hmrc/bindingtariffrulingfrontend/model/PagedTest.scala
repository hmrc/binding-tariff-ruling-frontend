package uk.gov.hmrc.bindingtariffrulingfrontend.model

import uk.gov.hmrc.play.test.UnitSpec

class PagedTest extends UnitSpec {

  "Paged" should {
    "calculate nonEmpty" in {
      Paged.empty.nonEmpty shouldBe false
      Paged(Seq(""), 1, 1).nonEmpty shouldBe true
    }
  }

}
