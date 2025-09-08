import sbt.*

object AppDependencies {

  private val hmrcMongoPlayVersion = "2.6.0"
  private val bootstrapVersion     = "10.1.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc.mongo"            %% "hmrc-mongo-play-30"         % hmrcMongoPlayVersion,
    "uk.gov.hmrc"                  %% "play-frontend-hmrc-play-30" % "12.10.0",
    "org.typelevel"                %% "cats-core"                  % "2.13.0",
    "com.digitaltangible"          %% "play-guard"                 % "3.0.0",
    "org.quartz-scheduler"          % "quartz"                     % "2.5.0",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.19.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion,
    "org.jsoup"          % "jsoup"                   % "1.20.1"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
