lazy val `play-se4-scratch` = project in file(".")

   name := "play-se4-scratch"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq("-encoding", "utf8")
scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlint")
scalacOptions  += "-language:postfixOps"

enablePlugins(PlayScala)

routesGenerator := InjectedRoutesGenerator

TaskKey[Unit]("stop") := {
  val pidFile = (stagingDirectory in Universal).value / "RUNNING_PID"
  if (!pidFile.exists) sys error "App not started!"
  val pid = IO read pidFile
  s"kill $pid".!
  println(s"Stopped application with process ID $pid")
}
