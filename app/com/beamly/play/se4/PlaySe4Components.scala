package com.beamly.play.se4

import akka.actor.ActorSystem
import com.beamly.play.se4.controllers.Se4Controller
import com.beamly.play.se4.healthchecks.HealthCheck
import com.beamly.play.se4.metrics.MetricsStore
import play.api.inject.ApplicationLifecycle

trait PlaySe4Components {
  def aServiceClass: Class[_]
  def runbookUrl: RunbookUrl
  def healthchecks: Iterable[HealthCheck]

  implicit def applicationLifecycle: ApplicationLifecycle
  implicit def actorSystem: ActorSystem

  lazy val metricsStore = new MetricsStore()
  lazy val se4Controller = new Se4Controller(aServiceClass, runbookUrl, metricsStore, healthchecks)
}
