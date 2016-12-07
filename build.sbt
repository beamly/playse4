lazy val playse4 = project in file(".")

organization := "com.beamly"
        name := "playse4"
    licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
 description := "Implementation of SE4 in Play"
    homepage := Some(url("https://github.com/beamly/playse4"))

enablePlugins(PlayScala)
disablePlugins(PlayLayoutPlugin)

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-encoding", "utf8")
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
scalacOptions  += "-language:higherKinds"
scalacOptions  += "-language:implicitConversions"
scalacOptions  += "-language:postfixOps"
scalacOptions  += "-Yinline-warnings"
scalacOptions  += "-Xfuture"
scalacOptions  += "-Yinline-warnings"
scalacOptions  += "-Yno-adapted-args"
scalacOptions  += "-Ywarn-dead-code"
scalacOptions  += "-Ywarn-numeric-widen"
scalacOptions  += "-Ywarn-value-discard"

maxErrors := 5
triggeredMessage := Watched.clearWhenTriggered

routesGenerator := InjectedRoutesGenerator

parallelExecution in Test := true
fork in Test := false

fork in run := true
cancelable in Global := true

bintrayOrganization := Some("beamly")

pomExtra := pomExtra.value ++ {
    <developers>
        <developer>
            <id>dwijnand</id>
            <name>Dale Wijnand</name>
            <email>dale wijnand gmail com</email>
            <url>dwijnand.com</url>
        </developer>
    </developers>
    <scm>
        <connection>scm:git:github.com/beamly/playse4.git</connection>
        <developerConnection>scm:git:git@github.com:beamly/playse4.git</developerConnection>
        <url>https://github.com/beamly/playse4</url>
    </scm>
}

releaseCrossBuild := true
GithubRelease.repo := s"beamly/${name.value}"

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
