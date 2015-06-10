package com.beamly.play

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NoStackTrace
import scala.util.{Failure, Success, Try}

case class FutureTimeoutException(duration: Duration) extends RuntimeException("Future has timed out after %s" format duration) with NoStackTrace

package object se4 {

  implicit final class FutureTimeoutW[A](val underlying: Future[A]) extends AnyVal {
    def timeout(duration: FiniteDuration)(implicit actorSystem: ActorSystem, executor: ExecutionContext): Future[A] = timeout(duration, Future failed FutureTimeoutException(duration))

    def timeout[B >: A](duration: FiniteDuration, onTimeout: => Future[B])(implicit actorSystem: ActorSystem, executor: ExecutionContext): Future[B] = {
      if (underlying.isCompleted) underlying
      else {
        val promise = Promise[B]()
        val timeout = actorSystem.scheduler.scheduleOnce(duration) {
          if (!promise.isCompleted) promise tryCompleteWith onTimeout
        }
        promise tryCompleteWith underlying
        promise.future onComplete (_ => timeout.cancel())
        promise.future
      }
    }
  }

  implicit final class TryW[T](val underlying: Try[T]) extends AnyVal {

    /**
     * Returns successful value from underlying [[scala.util.Try]] or attempts to convert exception to value.
     * @param pf Partial function to convert exceptions to a value
     * @tparam U type of return value
     * @return Underlying value or resulting value after converting exception
     */
    @inline
    def getOrRecover[U >: T](pf: => PartialFunction[Throwable, U]): U = underlying match {
      case Success(s) => s
      case Failure(e) => pf.applyOrElse(e, throw (_: Throwable))
    }
  }
}
