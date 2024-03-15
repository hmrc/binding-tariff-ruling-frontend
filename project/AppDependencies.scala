import sbt.*

object AppDependencies {

  private val hmrcMongoPlayVersion = "1.8.0"
  private val bootstrapVersion     = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "8.5.0",
    "org.typelevel"                %% "cats-core"                  % "2.10.0",
    "com.digitaltangible"          %% "play-guard"                 % "2.5.0",
    "org.quartz-scheduler"         % "quartz"                      % "2.3.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.17.0",
    "org.apache.pekko"             %% "pekko-connectors-csv"       % "1.0.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"          %% "bootstrap-test-play-30"  % bootstrapVersion,
    "org.wiremock"         % "wiremock-standalone"      % "3.4.2",
    "org.jsoup"            % "jsoup"                    % "1.17.2",
    "com.vladsch.flexmark" % "flexmark-all"             % "0.64.8",
    "org.scalatest"        %% "scalatest"               % "3.2.18",
    "org.mockito"          %% "mockito-scala-scalatest" % "1.17.30",
    "org.scalatestplus"    %% "scalacheck-1-17"         % "3.2.18.0",
    "uk.gov.hmrc.mongo"    %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
