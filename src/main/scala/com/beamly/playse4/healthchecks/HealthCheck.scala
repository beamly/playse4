package com.beamly.playse4
package healthchecks

import akka.actor.ActorSystem
import com.beamly.playse4.utils.AtomicFuture
import org.joda.time.{ DateTime, DateTimeZone }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try
import scala.util.control.NonFatal
import java.util.concurrent.atomic.AtomicInteger

sealed trait GTGStatus
case object GTGOkay   extends GTGStatus
case object GTGFailed extends GTGStatus

sealed abstract class TestStatus(val gtg: GTGStatus)
      case object TestInProgress                                                         extends TestStatus(GTGOkay)
      case object TestPassed                                                             extends TestStatus(GTGOkay)
final case class TestFailed(   description: String, exception: Option[Throwable] = None) extends TestStatus(GTGFailed)
final case class TestWarning(  description: String)                                      extends TestStatus(GTGOkay)
final case class TestCancelled(description: String)                                      extends TestStatus(GTGOkay)

final case class TestResult(name: String, timestamp: DateTime, duration: FiniteDuration, status: TestStatus)

abstract class HealthCheck(val testName: String, val testInterval: FiniteDuration = Duration(30, SECONDS))
  (implicit val actorSystem: ActorSystem)
{
  import actorSystem.dispatcher

  private val _lastTestResult: AtomicFuture[TestResult] = {
    val now = DateTime now DateTimeZone.UTC
    new AtomicFuture(Future successful TestResult(testName, now, Duration.Zero, TestInProgress))
  }

  protected val successFailCount = new AtomicInteger(0)

  def latestResult : Future[TestResult] = _lastTestResult.get
  def status       : Future[TestStatus] = latestResult map (_.status)
  def passed       : Future[Boolean]    = latestResult map (_.status == TestPassed)
  def gtgPassed    : Future[Boolean]    = latestResult map (_.status.gtg == GTGOkay)

  protected def performTest(): Future[TestStatus]

  final def invokeTest(): Future[Unit] =
    _lastTestResult flatSend { lastTestResult =>
      val startNanos = System.nanoTime()
      val startDate  = DateTime now DateTimeZone.UTC

      Try(
        performTest()
          recover { case NonFatal(e) => TestFailed(s"Uncaught exception: $e", exception = Some(e)) }
          timeout (testInterval, Future successful TestFailed(s"Test timed out after $testInterval"))
      ) getOrRecover {
        case NonFatal(e) => Future successful TestFailed(s"Uncaught exception: $e", exception = Some(e))
      } map { testStatus =>
        if (testStatus == TestPassed) successFailCount set 0 else successFailCount.incrementAndGet()
        TestResult(testName, startDate, Duration.fromNanos(System.nanoTime() - startNanos), testStatus)
      }
    }
}
