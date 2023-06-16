import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val scope = "test"

  private val hmrcMongoPlayVersion = "0.74.0"
  private val bootstrapVersion     = "7.15.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-allowlist-filter"      % "1.1.0",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "7.7.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.9.0",
    "com.digitaltangible"          %% "play-guard"                 % "2.5.0",
    "org.quartz-scheduler"         % "quartz"                      % "2.3.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.15.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % bootstrapVersion     % scope,
    "com.github.tomakehurst" % "wiremock-jre8"            % "2.35.0"             % scope,
    "com.typesafe.play"      %% "play-test"               % current              % scope,
    "org.jsoup"              % "jsoup"                    % "1.16.1"             % scope,
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.64.4"             % scope,
    "org.scalatest"          %% "scalatest"               % "3.2.15"             % scope,
    "org.mockito"            %% "mockito-scala-scalatest" % "1.17.14"            % scope,
    "org.scalatestplus"      %% "scalacheck-1-17"         % "3.2.15.0"           % scope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28" % hmrcMongoPlayVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
