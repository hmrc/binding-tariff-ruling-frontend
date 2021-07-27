import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-27" % "5.6.0",
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-play-27"         % "0.51.0",
    "uk.gov.hmrc"          %% "play-json-union-formatter"  % "1.13.0-play-27",
    "uk.gov.hmrc"          %% "play-allowlist-filter"      % "1.0.0-play-27",
    "uk.gov.hmrc"          %% "play-frontend-govuk"        % "0.79.0-play-27",
    "uk.gov.hmrc"          %% "play-frontend-hmrc"         % "0.79.0-play-27",
    "org.typelevel"        %% "cats-core"                  % "2.2.0",
    "com.digitaltangible"  %% "play-guard"                 % "2.4.0",
    "org.quartz-scheduler" % "quartz"                      % "2.3.2"
  )

  val scope = "test"

  val jettyVersion = "9.4.27.v20200227"

  val test = Seq(
    "com.github.tomakehurst" % "wiremock"                  % "2.27.2"         % scope,
    "com.typesafe.play"      %% "play-test"                % current          % scope,
    "org.mockito"            % "mockito-core"              % "2.28.2"         % scope,
    "org.jsoup"              % "jsoup"                     % "1.13.1"         % scope,
    "org.pegdown"            % "pegdown"                   % "1.6.0"          % scope,
    "org.scalatest"          %% "scalatest"                % "3.0.9"          % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "4.0.3"          % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.14.3"         % scope,
    "uk.gov.hmrc"            %% "http-verbs-test"          % "1.8.0-play-27"  % scope,
    "uk.gov.hmrc"            %% "service-integration-test" % "1.1.0-play-27"  % scope,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-27"  % "0.51.0"         % scope,
    //Need to peg this version of jetty to prevent binary compatibility errors
    "org.eclipse.jetty" % "jetty-server"  % jettyVersion % scope,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % scope
  )

}
