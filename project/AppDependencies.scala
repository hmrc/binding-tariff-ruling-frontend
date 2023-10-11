import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  val scope = "test"

  private val hmrcMongoPlayVersion = "1.3.0"
  private val bootstrapVersion     = "7.22.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"            % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-allowlist-filter-play-28" % "1.2.0",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"            % "7.23.0-play-28",
    "org.typelevel"                %% "cats-core"                     % "2.10.0",
    "com.digitaltangible"          %% "play-guard"                    % "2.5.0",
    "org.quartz-scheduler"         % "quartz"                         % "2.3.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"          % "2.15.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-28"  % bootstrapVersion,
    "org.wiremock"         % "wiremock-standalone"      % "3.2.0",
    "com.typesafe.play"    %% "play-test"               % current,
    "org.jsoup"            % "jsoup"                    % "1.16.1",
    "com.vladsch.flexmark" % "flexmark-all"             % "0.64.8",
    "org.scalatest"        %% "scalatest"               % "3.2.17",
    "org.mockito"          %% "mockito-scala-scalatest" % "1.17.27",
    "org.scalatestplus"    %% "scalacheck-1-17"         % "3.2.17.0",
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-28" % hmrcMongoPlayVersion
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
