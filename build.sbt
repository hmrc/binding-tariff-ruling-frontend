import sbt.Keys.name
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "binding-tariff-ruling-frontend"

lazy val plugins: Seq[Plugins] =
  Seq(PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val microservice = (project in file("."))
  .enablePlugins(plugins: _*)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(majorVersion := 0)
  .settings(PlayKeys.playDefaultPort := 9586)
  .settings(
    name := appName,
    scalaVersion := "2.12.12",
    targetJvm := "jvm-1.8",
    libraryDependencies ++= (AppDependencies.compile ++ AppDependencies.test).map(_ withSources ()),
    evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    parallelExecution in Test := false,
    fork in Test := true,
    retrieveManaged := true,
    // Use the silencer plugin to suppress warnings from unused imports in compiled twirl templates
    scalacOptions += "-P:silencer:pathFilters=views;routes",
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.1" cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % "1.7.1" % Provided cross CrossVersion.full
    )
  )
  .settings(
    unmanagedSourceDirectories in Test := Seq(
      (baseDirectory in Test).value / "test/unit",
      (baseDirectory in Test).value / "test/util"
    ),
    resourceDirectory in Test := baseDirectory.value / "test" / "resources",
    addTestReportOption(Test, "test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "javascripts/jquery.min.js",
          "javascripts/back-link.js",
          "javascripts/app.js"
        ))
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    pipelineStages in Assets := Seq(concat)
    // only compress files generated by concat
  )

// Coverage configuration
coverageMinimum := 90
coverageFailOnMinimum := true
coverageExcludedPackages := "<empty>;com.kenshoo.play.metrics.*;prod.*;testOnlyDoNotUseInAppConf.*;app.*;uk.gov.hmrc.BuildInfo"
