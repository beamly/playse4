package com.beamly.playse4

trait PlayJsonAllInstances
  extends PlayJsonJavaInstances
     with PlayJsonJodaTimeInstances

object PlayJsonAllInstances extends PlayJsonAllInstances
