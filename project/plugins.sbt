// For sbt-github-release
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"

addSbtPlugin("me.lessis"         % "bintray-sbt"        % "0.3.0")
addSbtPlugin("ohnosequences"     % "sbt-github-release" % "0.3.0")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"         % "2.4.2")
addSbtPlugin("com.github.gseitz" % "sbt-release"        % "1.0.0")
