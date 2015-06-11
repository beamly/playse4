package com.beamly.play.se4
package healthchecks

import org.joda.time.{ DateTime, DateTimeZone }
import play.api.libs.json._

case class HealthcheckResponse(
  report_as_of    : DateTime,
  report_duration : String,
  tests           : Iterable[TestResultResponse]
)

object HealthcheckResponse {
  implicit val jsonFormat: Format[HealthcheckResponse] = Json.format[HealthcheckResponse]

  def create(reportDuration: String, testResults: Iterable[TestResult])
  : HealthcheckResponse = {
    val testResultResponses =
      testResults map (r =>
        TestResultResponse(r.name, r.timestamp, r.duration.toMillis, format(r.status), desc(r.status))
      )
    HealthcheckResponse(DateTime now DateTimeZone.UTC, reportDuration, testResultResponses)
  }

  private def format(ts: TestStatus) =
    ts match {
      case TestInProgress   => "inprogress"
      case TestPassed       => "passed"
      case _: TestFailed    => "failed"
      case _: TestWarning   => "warning"
      case _: TestCancelled => "cancelled"
    }

  private def desc(ts: TestStatus) =
    ts match {
      case t: TestFailed    => Some(t.description)
      case t: TestWarning   => Some(t.description)
      case t: TestCancelled => Some(t.description)
      case _                => None
    }
}

case class TestResultResponse(
  test_name       : String,
  tested_at       : DateTime,
  duration_millis : Double,
  test_result     : String,
  description     : Option[String]
  )

object TestResultResponse {
  implicit val jsonFormat: Format[TestResultResponse] = Json.format[TestResultResponse]
}
