ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.5.2"

lazy val microservice = Project("binding-tariff-ruling-frontend", file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies(),
    PlayKeys.playDefaultPort := 9586,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:msg=unused import&src=conf/.*:s",
      "-Wconf:msg=unused import&src=views/.*:s",
      "-Wconf:src=routes/.*:s"
    )
  )
  .settings(
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(
          Seq(
            "javascripts/jquery.min.js",
            "javascripts/back-link.js"
          )
        )
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat)
    // only compress files generated by concat
  )
  .settings(CodeCoverageSettings())

addCommandAlias("scalafmtAll", "all scalafmtSbt scalafmt Test/scalafmt")
