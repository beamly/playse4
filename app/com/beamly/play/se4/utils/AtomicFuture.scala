package com.beamly.play.se4
package utils

import scala.annotation.tailrec
import scala.concurrent.{ ExecutionContext, Future, Promise }
import java.util.concurrent.atomic.AtomicReference

final class AtomicFuture[T](initialValue: Future[T]) extends AtomicReference[Future[T]](initialValue) {
  @tailrec def send(f: T => T)(implicit executor: ExecutionContext): Future[Unit] = {
    val current = get()
    val promise = Promise[T]()
    if (compareAndSet(current, promise.future)) {
      promise completeWith (current map f)
      promise.future map (_ => ())
    } else send(f)
  }

  @tailrec def flatSend(f: T => Future[T])(implicit executor: ExecutionContext): Future[Unit] = {
    val current = get()
    val promise = Promise[T]()
    if (compareAndSet(current, promise.future)) {
      promise completeWith (current flatMap f)
      promise.future map (_ => ())
    } else flatSend(f)
  }
}

