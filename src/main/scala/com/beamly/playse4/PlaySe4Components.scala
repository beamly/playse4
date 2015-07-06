package com.beamly.playse4

import akka.actor.ActorSystem
import com.beamly.playse4.controllers.Se4Controller
import com.beamly.playse4.healthchecks.HealthCheck
import com.beamly.playse4.metrics.MetricsStore
import play.api.Configuration
import play.api.inject.ApplicationLifecycle

trait PlaySe4Components {
  def aServiceClass: Class[_]
  def runbookUrl: RunbookUrl
  def healthchecks: Iterable[HealthCheck]

  implicit def applicationLifecycle: ApplicationLifecycle
  implicit def actorSystem: ActorSystem
  def configuration: Configuration

  lazy val metricsStore = new MetricsStore()
  lazy val se4Controller =
    new Se4Controller(configuration.underlying, aServiceClass, runbookUrl, healthchecks, metricsStore)
}
