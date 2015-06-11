package com.beamly.play.se4

trait PlayJsonAllInstances
  extends PlayJsonJavaInstances
     with PlayJsonJodaTimeInstances

object PlayJsonAllInstances extends PlayJsonAllInstances
