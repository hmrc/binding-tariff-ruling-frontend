import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-27" % "3.2.0",
    "uk.gov.hmrc"          %% "simple-reactivemongo"       % "7.31.0-play-27",
    "uk.gov.hmrc"          %% "play-json-union-formatter"  % "1.12.0-play-27",
    "uk.gov.hmrc"          %% "play-allowlist-filter"      % "0.2.0-play-27",
    "uk.gov.hmrc"          %% "play-frontend-govuk"        % "0.56.0-play-27",
    "uk.gov.hmrc"          %% "play-frontend-hmrc"         % "0.34.0-play-27",
    "uk.gov.hmrc"          %% "mongo-lock"                 % "7.0.0-play-27",
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
    "org.scalatest"          %% "scalatest"                % "3.2.6"          % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"       % "3.1.3"          % scope,
    "org.scalacheck"         %% "scalacheck"               % "1.14.3"         % scope,
    "uk.gov.hmrc"            %% "http-verbs-test"          % "1.8.0-play-27"  % scope,
    "uk.gov.hmrc"            %% "service-integration-test" % "0.13.0-play-27" % scope,
    "uk.gov.hmrc"            %% "reactivemongo-test"       % "4.22.0-play-27" % scope,
    //Need to peg this version of jetty to prevent binary compatibility errors
    "org.eclipse.jetty" % "jetty-server"  % jettyVersion % scope,
    "org.eclipse.jetty" % "jetty-servlet" % jettyVersion % scope
  )

}
