import sbt.Setting
import scoverage.ScoverageKeys.*

object CodeCoverageSettings {
  val settings: Seq[Setting[?]] = Seq(
    coverageExcludedFiles := ".*Routes.*",
    coverageMinimumStmtTotal := 94,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )
}
