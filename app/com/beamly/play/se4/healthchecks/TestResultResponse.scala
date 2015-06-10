package com.beamly.play.se4
package healthchecks

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class TestResultResponse(testName: String, testedAt: DateTime, durationMillis: Double, testResult: String, description: Option[String])

object TestResultResponse {
  implicit val jsonFormat: Format[TestResultResponse] = (
      (__ \ "test_name").format[String] and
      (__ \ "tested_at").format[DateTime] and
      (__ \ "duration_millis").format[Double] and
      (__ \ "test_result").format[String] and
      (__ \ "description").formatNullable[String]
    )(TestResultResponse.apply, unlift(TestResultResponse.unapply))
}
