import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val scope = "test"

  private val hmrcMongoPlayVersion = "0.74.0"
  private val bootstrapVersion     = "7.13.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-allowlist-filter"      % "1.1.0",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "6.6.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.9.0",
    "com.digitaltangible"          %% "play-guard"                 % "2.5.0",
    "org.quartz-scheduler"         % "quartz"                      % "2.3.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.14.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion     % scope,
    "com.github.tomakehurst" % "wiremock"                 % "2.33.2"             % scope,
    "com.typesafe.play"      %% "play-test"               % current              % scope,
    "org.jsoup"              % "jsoup"                    % "1.15.4"             % scope,
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.62.2"             % scope,
    "org.scalatest"          %% "scalatest"               % "3.2.15"             % scope,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.12"            % scope,
    "org.scalatestplus"      %% "scalacheck-1-17"         % "3.2.15.0"           % scope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoPlayVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
