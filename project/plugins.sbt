resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)
resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.typesafe.play"   % "sbt-plugin"             % "2.8.15")
addSbtPlugin("net.virtual-void"    % "sbt-dependency-graph"   % "0.10.0-RC1")
addSbtPlugin("io.github.irundaia"  % "sbt-sassify"            % "1.5.2")
addSbtPlugin("org.scalastyle"      %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scoverage"       % "sbt-scoverage"          % "2.0.0")
addSbtPlugin("uk.gov.hmrc"         % "sbt-auto-build"         % "3.8.0")
addSbtPlugin("uk.gov.hmrc"         % "sbt-distributables"     % "2.1.0")
addSbtPlugin("com.timushev.sbt"    % "sbt-updates"            % "0.6.3")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"           % "2.4.6")
addSbtPlugin("com.typesafe.sbt"    % "sbt-digest"             % "1.1.4")
addSbtPlugin("net.ground5hark.sbt" % "sbt-concat"             % "0.2.0")
