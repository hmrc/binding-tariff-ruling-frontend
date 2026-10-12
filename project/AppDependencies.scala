import sbt.*

object AppDependencies {

  private val hmrcMongoPlayVersion = "2.14.0"
  private val bootstrapVersion     = "10.8.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "13.16.0",
    "org.typelevel"                %% "cats-core"                  % "2.13.0",
    "com.digitaltangible"          %% "play-guard"                 % "3.0.0",
    "org.quartz-scheduler"          % "quartz"                     % "2.5.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.22.3.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion,
    "org.jsoup"          % "jsoup"                   % "1.23.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
