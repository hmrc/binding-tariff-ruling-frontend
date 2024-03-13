import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {
  val settings: Seq[Setting[?]] = Seq(
    coverageExcludedFiles := "<empty>;prod.*;testOnlyDoNotUseInAppConf.*;app.*;uk.gov.hmrc.BuildInfo",
    coverageMinimumStmtTotal := 90,
    coverageFailOnMinimum := true
  )
}
