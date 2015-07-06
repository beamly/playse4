package com.beamly.playse4

import org.joda.time.{ Duration => JodaDuration }
import play.api.libs.json._

import scala.concurrent.duration._
import java.util.concurrent.TimeUnit

trait PlayJsonJodaTimeInstances {
  implicit val dateTimeFormat =
    Format(
      Reads  jodaDateReads  "yyyy-MM-dd'T'HH:mm:ss.SSSZZ",
      Writes jodaDateWrites "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
    )

  implicit val durationFormat = new Format[JodaDuration] {
    def writes(d: JodaDuration): JsValue =
      Json toJson Duration(d.getMillis, TimeUnit.MILLISECONDS).toString

    def reads(json: JsValue): JsResult[JodaDuration] =
      json.validate[String] map (Duration(_)) map (_.toMillis) map JodaDuration.millis
  }
}

object PlayJsonJodaTimeInstances extends PlayJsonJodaTimeInstances
