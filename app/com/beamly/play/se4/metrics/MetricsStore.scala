package com.beamly.play.se4
package metrics

import akka.actor.{ Actor, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout._

import scala.concurrent.Future
import scala.concurrent.duration._

sealed trait MetricEvent

sealed trait MetricWriteEvent extends MetricEvent
final case class IncMetricCounter(metricId: String, delta: BigInt) extends MetricWriteEvent
final case class DecMetricCounter(metricId: String, delta: BigInt) extends MetricWriteEvent
final case class SetMetricCounter(metricId: String, value: BigInt) extends MetricWriteEvent

case object GetMetricsSnapshot extends MetricEvent
final case class MetricsSnapshot(counters: Map[String, BigInt])

class MetricsStoreActor private () extends Actor {
  private var counters = Map.empty[String, BigInt]

  def receive = {
    case metricEvent: MetricEvent =>
      metricEvent match {
        case IncMetricCounter(id, delta) => counters += id -> (counters.getOrElse(id, BigInt(0)) + delta)
        case DecMetricCounter(id, delta) => counters += id -> (counters.getOrElse(id, BigInt(0)) - delta)
        case SetMetricCounter(id, value) => counters += id -> value
        case GetMetricsSnapshot          => sender ! MetricsSnapshot(counters)
      }
  }
}
object MetricsStoreActor {
  def props: Props = Props(new MetricsStoreActor)
}

trait MetricsStoreWriter {
  def incCounter(metricId: String, delta: BigInt = 1) : Unit
  def decCounter(metricId: String, delta: BigInt = 1) : Unit
  def setCounter(metricId: String, value: BigInt)     : Unit
}

trait MetricsStoreReader {
  def getCountersSnapshot(): Future[Map[String, BigInt]]
}

class MetricsStore(implicit actorSystem: ActorSystem) extends MetricsStoreWriter with MetricsStoreReader {
  import actorSystem.dispatcher

  private val actor = actorSystem.actorOf(MetricsStoreActor.props, "metrics-actor")

  def incCounter(metricId: String, delta: BigInt = 1) : Unit = actor ! IncMetricCounter(metricId, delta)
  def decCounter(metricId: String, delta: BigInt = 1) : Unit = actor ! DecMetricCounter(metricId, delta)
  def setCounter(metricId: String, value: BigInt)     : Unit = actor ! SetMetricCounter(metricId, value)

  def getCountersSnapshot(): Future[Map[String, BigInt]] =
    actor.ask(GetMetricsSnapshot)(5.seconds).mapTo[MetricsSnapshot] map (_.counters)
}
