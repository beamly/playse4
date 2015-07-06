package com.beamly

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success, Try }
import scala.{ PartialFunction => ?=> }

package object playse4 extends PlayJsonAllInstances {
  implicit final class FutureTimeoutW[A](val future: Future[A]) extends AnyVal {
    def timeout[B >: A](duration: FiniteDuration, onTimeout: => Future[B])
      (implicit actorSystem: ActorSystem, ec: ExecutionContext)
    : Future[B] = {
      if (future.isCompleted)
        future
      else {
        val promise = Promise[B]()
        val timeout = actorSystem.scheduler.scheduleOnce(duration) {
          if (!promise.isCompleted) promise tryCompleteWith onTimeout
          ()
        }
        promise tryCompleteWith future
        promise.future onComplete (_ => timeout.cancel())
        promise.future
      }
    }
  }

  implicit final class TryW[T](val try0: Try[T]) extends AnyVal {
    @inline def getOrRecover[U >: T](pf: => Throwable ?=> U): U =
      try0 match {
        case Success(x) => x
        case Failure(t) => pf.applyOrElse(t, throw _: Throwable)
      }
  }
}
