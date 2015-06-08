lazy val `play-se4-scratch` = project in file(".")

   name := "play-se4-scratch"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.6"

enablePlugins(PlayScala)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
