import sbt.*

object AppDependencies {

  private val hmrcMongoPlayVersion = "2.3.0"
  private val bootstrapVersion     = "9.5.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "11.6.0",
    "org.typelevel"                %% "cats-core"                  % "2.12.0",
    "com.digitaltangible"          %% "play-guard"                 % "3.0.0",
    "org.quartz-scheduler"          % "quartz"                     % "2.5.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.18.2",
    "org.apache.pekko"             %% "pekko-connectors-csv"       % "1.0.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "org.jsoup"          % "jsoup"                   % "1.18.3",
    "org.scalatestplus" %% "scalacheck-1-17"         % "3.2.18.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
