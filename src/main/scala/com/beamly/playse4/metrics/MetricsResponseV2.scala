package com.beamly.playse4
package metrics

import org.joda.time.DateTime
import play.api.libs.json.{ Format, Json }

case class MetricsResponseV2(when_dtm: DateTime, metrics: Map[String, BigDecimal])
object MetricsResponseV2 {
  implicit val jsonFormat: Format[MetricsResponseV2] = Json.format[MetricsResponseV2]
}
