lazy val playse4 = project in file(".")

organization := "com.beamly"
        name := "playse4"
    licenses := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
 description := "Implementation of SE4 in Play"

enablePlugins(PlayScala)
disablePlugins(PlayLayoutPlugin)

      scalaVersion := "2.11.7"
crossScalaVersions := Seq(scalaVersion.value)

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
GithubRelease.repo := s"beamly/${name.value}"

val createGithubRelease =
  Def setting
    ReleaseStep(
      check  = releaseStepTaskAggregated(checkGithubCredentials in thisProjectRef.value),
      action = releaseStepTaskAggregated(       releaseOnGithub in thisProjectRef.value)
    )

releaseProcess := {
  import ReleaseTransformations._

  Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges,
    createGithubRelease.value
  )
}

watchSources ++= (baseDirectory.value * "*.sbt").get
watchSources ++= (baseDirectory.value / "project" * "*.scala").get
