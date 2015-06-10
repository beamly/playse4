package com.beamly.play.se4
package util

import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology

/**
 * Helpers for creating UTC DateTimes
 */
object Time {
  val defaultChronology = ISOChronology.getInstanceUTC

  def now() = DateTime.now(defaultChronology)

  def apply() = now()

  def apply(millis: Long) = new DateTime(millis, defaultChronology)
}