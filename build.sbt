lazy val `play-se4-scratch` = project in file(".")

   name := "play-se4-scratch"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies += "com.lihaoyi" % "ammonite-repl" % "0.3.2" % "test" cross CrossVersion.full

enablePlugins(PlayScala)

initialCommands in (Test, console) := """ammonite.repl.Repl.run("")"""

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

TaskKey[Unit]("stop") := {
  val pidFile = (stagingDirectory in Universal).value / "RUNNING_PID"
  if (!pidFile.exists) sys error "App not started!"
  val pid = IO read pidFile
  s"kill $pid".!
  println(s"Stopped application with process ID $pid")
}
