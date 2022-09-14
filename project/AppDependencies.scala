import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val scope = "test"

  private val hmrcMongoPlayVersion = "0.72.0"
  private val silencerVersion      = "1.7.9"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-28" % "7.3.0",
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-28"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-json-union-formatter"  % "1.15.0-play-28",
    "uk.gov.hmrc"                  %% "play-allowlist-filter"      % "1.1.0",
    "uk.gov.hmrc"                  %% "play-frontend-hmrc"         % "3.24.0-play-28",
    "org.typelevel"                %% "cats-core"                  % "2.8.0",
    "com.digitaltangible"          %% "play-guard"                 % "2.5.0",
    "org.quartz-scheduler"         % "quartz"                      % "2.3.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.13.4",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock"                  % "2.33.2"             % scope,
    "com.typesafe.play"      %% "play-test"                % current              % scope,
    "org.mockito"            % "mockito-core"              % "4.8.0"              % scope,
    "org.jsoup"              % "jsoup"                     % "1.15.3"             % scope,
    "com.vladsch.flexmark"   % "flexmark-all"              % "0.62.2"             % scope,
    "org.scalatest"          %% "scalatest"                % "3.2.13"             % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"              % scope,
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.10.0"           % scope,
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.11.0"           % scope,
    "uk.gov.hmrc"            %% "service-integration-test" % "1.3.0-play-28"      % scope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % hmrcMongoPlayVersion % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
