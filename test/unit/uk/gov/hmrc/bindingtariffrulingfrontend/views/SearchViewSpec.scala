package uk.gov.hmrc.bindingtariffrulingfrontend.views

class SearchViewSpec extends ViewSpec {

  override protected def testMessages: Map[String, Map[String, String]] =
    Map(
      "default" -> Map(
        "search.summary.nomatch.landing"  -> "There are no rulings.",
        "search.summary.onematch.landing" -> "Showing 1 result.",
        "search.summary.onepage.landing"  -> "Showing {0} results.",
        "search.summary.manypage.landing" -> "Showing {0} to {1} of {2} results."
      ))
}
