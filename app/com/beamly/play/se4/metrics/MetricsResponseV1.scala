package com.beamly.play.se4
package metrics

import org.joda.time.DateTime
import play.api.libs.json.{ Format, Json }

case class MetricsResponseV1(when_dtm: DateTime, metrics: Map[String, CounterV1])
object MetricsResponseV1 {
  implicit val jsonFormat: Format[MetricsResponseV1] = Json.format[MetricsResponseV1]
}

case class CounterV1(metric_name: String, metric_type: String, count: BigDecimal)
object CounterV1 {
  implicit val jsonFormat: Format[CounterV1] = Json.format[CounterV1]
}
