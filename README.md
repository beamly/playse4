# [playse4][] [![Apache 2 badge][]](http://www.apache.org/licenses/LICENSE-2.0)

  [playse4]: https://github.com/beamly/playse4
  [Apache 2 badge]: http://img.shields.io/:license-Apache%202-red.svg
  [SE4]: https://github.com/beamly/SE4
  [Play]: https://playframework.com/

`playse4` is an implementation of [SE4][] in [Play][].

## Setup

Add this to your sbt build (`build.sbt`):

    libraryDependencies += "com.beamly" %% "playse4" % "0.3.0"

Add this to your routes:

    ->  /service se4.Routes

Mix `com.beamly.playse4.PlaySe4Components` into your components cake and configure the required dependencies
and routes.

Here's a minimal example:

    class AppComponents(context: Context)
      extends play.api.BuiltInComponentsFromContext(context)
         with com.beamly.playse4.PlaySe4Components {
      Logger configure context.environment

      val aServiceClass = getClass
      val runbookUrl    = new com.beamly.playse4.RunbookUrl(new URI("https://acme.com/runbook/myapp"))
      val healthchecks  = Nil // TODO: Write at least one healthcheck

      lazy val router = new Routes(httpErrorHandler, se4Routes)
    }

## Dependencies

* Scala 2.11.x
* Play 2.5.x

## Licence

Copyright 2015 Dale Wijnand

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
