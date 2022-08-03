import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val mongoVersion = "0.68.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28" % "6.2.0",
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-28"         % mongoVersion,
    "uk.gov.hmrc"          %% "play-json-union-formatter"  % "1.15.0-play-28",
    "uk.gov.hmrc"          %% "play-allowlist-filter"      % "1.0.0-play-28",
    "uk.gov.hmrc"          %% "play-frontend-hmrc"         % "3.21.0-play-28",
    "org.typelevel"        %% "cats-core"                  % "2.7.0",
    "com.digitaltangible"  %% "play-guard"                 % "2.5.0",
    "org.quartz-scheduler" % "quartz"                      % "2.3.2"
  )

  val scope = Test

  val jettyVersion = "9.4.27.v20200227"

  val test: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock"                  % "2.27.2"         % scope,
    "com.typesafe.play"      %% "play-test"                % current          % scope,
    "org.mockito"            % "mockito-core"              % "3.11.2"         % scope,
    "org.jsoup"              % "jsoup"                     % "1.14.1"         % scope,
    "org.pegdown"            % "pegdown"                   % "1.6.0"          % scope,
    "org.scalatest"          %% "scalatest"                % "3.2.9"          % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"          % scope,
    "org.scalatestplus"      %% "mockito-3-4"              % "3.2.9.0"        % scope,
    "org.scalatestplus"      %% "scalacheck-1-15"          % "3.2.9.0"        % scope,
    "uk.gov.hmrc"            %% "service-integration-test" % "1.1.0-play-28"  % scope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"  % mongoVersion     % scope,
    "org.eclipse.jetty"      % "jetty-server"              % jettyVersion     % scope,
    "org.eclipse.jetty"      % "jetty-servlet"             % jettyVersion     % scope
  )

  def apply(): Seq[ModuleID] = compile ++ test

}
