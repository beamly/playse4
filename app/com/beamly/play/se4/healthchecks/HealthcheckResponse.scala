package com.beamly.play.se4
package healthchecks

import com.beamly.play.se4.healthcheck._
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class HealthcheckResponse(reportAsOf: DateTime, reportDuration: String, testResponses: Iterable[TestResultResponse])

object HealthcheckResponse {

  implicit val jsonFormat = (
    (__ \ "report_as_of").format[DateTime] and
    (__ \ "report_duration").format[String] and
    (__ \ "tests").format[Iterable[TestResultResponse]]
  )(HealthcheckResponse.apply, unlift(HealthcheckResponse.unapply))

  def factory( reportAsOf:DateTime, reportDuration:String, tests:Iterable[TestResult] ) : HealthcheckResponse = {
    def format( s: TestStatus ) = s match {
      case TestInProgress    => "inprogress"
      case TestPassed        => "passed"
      case _: TestFailed     => "failed"
      case _: TestWarning    => "warning"
      case _: TestCancelled  => "cancelled"
    }

    def description(s: TestStatus) = s match {
      case t: TestFailed    => Some(t.description)
      case t: TestWarning   => Some(t.description)
      case t: TestCancelled => Some(t.description)
      case _                => None
    }

    val testResultDTOs = tests.map(r => TestResultResponse(r.name, r.timestamp, r.duration.toMillis, format(r.status), description(r.status)))
    HealthcheckResponse(reportAsOf, reportDuration, testResultDTOs)
  }
}
