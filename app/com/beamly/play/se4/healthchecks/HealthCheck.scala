package com.beamly.play.se4
package healthcheck

import akka.actor.ActorSystem
import com.beamly.play.se4.util.{AtomicFuture, Time}
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try
import java.util.concurrent.atomic.AtomicInteger

case class TestResult(name: String, timestamp:DateTime, duration:FiniteDuration, status: TestStatus)

sealed trait GTGStatus
case object GTGFailed extends GTGStatus
case object GTGOkay extends GTGStatus

sealed trait TestStatus {
  def gtg: GTGStatus
}
case object TestInProgress extends TestStatus {  val gtg = GTGOkay }
case object TestPassed extends TestStatus { val gtg = GTGOkay }
case class TestFailed(description: String, gtg: GTGStatus = GTGFailed, exception: Option[Throwable] = None) extends TestStatus
case class TestWarning(description: String, gtg: GTGStatus = GTGOkay) extends TestStatus
case class TestCancelled(description: String, gtg: GTGStatus = GTGOkay) extends TestStatus

/**
 * An individual health check that can be registered with HealthCheckResource.
 */
abstract class HealthCheck(val testName:String, val testInterval: FiniteDuration=Duration(30, SECONDS))(implicit executor: ExecutionContext) {
  private val _lastTestResult = new AtomicFuture(Future successful TestResult(testName, Time(), Duration.Zero, TestInProgress))

  protected val successFailCount = new AtomicInteger(0)

  def latestResult : Future[TestResult] = _lastTestResult.get

  def status : Future[TestStatus] = latestResult map (_.status)

  def passed : Future[Boolean] = latestResult map (_.status == TestPassed)

  def gtgPassed : Future[Boolean] = latestResult map (_.status.gtg == GTGOkay)

  protected def performTest() : Future[TestStatus]

  // TODO: Add gauges for test results and times DW 2013-01-08
  final def invokeTest()(implicit actorSystem: ActorSystem): Future[Unit] = _lastTestResult flatSend { lastTestResult =>
    val startNanos = System.nanoTime()
    val startDate  = Time()

    Try {
      performTest() recover {
        case ex => TestFailed(s"Uncaught exception: $ex", exception = Some(ex))
      } timeout (testInterval, Future successful TestFailed(s"Test timed out after $testInterval"))
    } getOrRecover {
      case ex => Future successful TestFailed(s"Uncaught exception: $ex", exception = Some(ex))
    } map { testStatus =>
      if (testStatus == TestPassed) {
        successFailCount set 0
      } else {
        successFailCount.incrementAndGet()
      }
      TestResult(testName, startDate, Duration.fromNanos(System.nanoTime() - startNanos), testStatus)
    }
  }
}

object HealthCheck {
  val TEST_PASSED     = "passed"
  val TEST_FAILED     = "failed"
  val TEST_WARNING    = "warning"
  val TEST_INPROGRESS = "inprogress"
  val TEST_CANCELLED  = "cancelled"
}
