package com.beamly.playse4
package controllers

import akka.actor.{ ActorSystem, Cancellable }
import com.beamly.playse4.healthchecks.{ HealthCheck, HealthcheckResponse, TestPassed }
import com.beamly.playse4.metrics.{ CounterV1, MetricsResponseV1, MetricsResponseV2, MetricsStore }
import com.beamly.playse4.status.Se4StatusResponse
import com.beamly.playse4.utils.JarManifest
import com.typesafe.config.{ Config, ConfigRenderOptions }
import org.joda.time.{ DateTime, DateTimeZone, Duration => JodaDuration }
import play.Logger
import play.api.inject.ApplicationLifecycle
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent, Controller }

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.duration._

// TODO: Restrict to return only json
class Se4Controller(
  config        : Config,
  aServiceClass : Class[_],
  runbookUrl    : RunbookUrl,
  healthchecks  : Iterable[HealthCheck],
  metricsStore  : MetricsStore
)(implicit actorSystem: ActorSystem, applicationLifecycle: ApplicationLifecycle) extends Controller {

  import actorSystem.dispatcher

  scheduleHealthChecks()
  applicationLifecycle addStopHook (() => Future successful unscheduleHealthChecks())

  private var scheduledTests = Iterable.empty[Cancellable]

  private def scheduleHealthChecks(): Unit = {
    scheduledTests = healthchecks map { healthCheck =>
      actorSystem.scheduler.schedule(Duration.Zero, healthCheck.testInterval) {
        healthCheck.invokeTest()
        healthCheck.latestResult foreach { result =>
          import result._
          if (status == TestPassed)
            Logger debug s"HealthCheck [$name] $status"
          else
            Logger info s"HealthCheck [$name] $status"
        }
      }
    }
  }

  private def unscheduleHealthChecks(): Unit = {
    scheduledTests foreach (_.cancel())
    scheduledTests = Iterable.empty
  }

  def getServiceStatus: Action[AnyContent] = {
    val manifest = JarManifest fromClass aServiceClass getOrElse Map.empty withDefault(_ => "n/a")

    val      osMBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean
    val runTimeMBean = java.lang.management.ManagementFactory.getRuntimeMXBean

    val hostName    = java.net.InetAddress.getLocalHost.getHostName
    val hostAddress = java.net.InetAddress.getLocalHost.getHostAddress

    val currentDateTime = DateTime now DateTimeZone.UTC
    val upSince = new DateTime(runTimeMBean.getStartTime, DateTimeZone.UTC)

    val serviceStatusData =
      Se4StatusResponse(
        group_id         = manifest("Group-Id"),
        artifact_id      = manifest("Artifact-Id"),
        version          = manifest("Version"),
        git_sha1         = manifest("Git-SHA1"),

        built_by         = manifest("Built-By"),
        build_number     = manifest("Build-Number"),
        build_machine    = manifest("Build-Machine"),
        built_when       = manifest("Built-When"),
        compiler_version = manifest("Build-Jdk"),

        machine_name     = s"$hostName ($hostAddress)",
        current_time     = currentDateTime,
        up_since         = upSince,
        up_duration      = new JodaDuration(upSince, currentDateTime),

        os_arch          = osMBean.getArch,
        os_numprocessors = osMBean.getAvailableProcessors.toString,
        os_name          = osMBean.getName,
        os_version       = osMBean.getVersion,
        os_avgload       = osMBean.getSystemLoadAverage.toString,

        vm_name          = runTimeMBean.getVmName,
        vm_vendor        = runTimeMBean.getVmVendor,
        vm_version       = runTimeMBean.getVmVersion,
        runbook_url      = runbookUrl.value
    )

    Action(Ok(Json toJson serviceStatusData))
  }

  def getServiceHealthcheckGtg =
    Action.async(
      healthchecks
        .foldLeft(Future successful true) { (accGtgFuture, healthCheck) =>
          healthCheck.gtgPassed flatMap (healthCheckGtg => accGtgFuture map (accGtg => accGtg && healthCheckGtg))
        }
        .map {
          case true  => Ok("\"OK\"")
          case false => InternalServerError("\"FAILED\"")
        }
    )

  def getServiceHealthcheck =
    Action.async(
      Future.traverse(healthchecks)(_.latestResult)
        map (results => Ok(Json toJson HealthcheckResponse.create("0 seconds", results)))
    )

  def getServiceMetricsV1 =
    Action.async(
      getServiceMetricsData()
        .map(data => data map (kv => kv._1 -> CounterV1(kv._1, "counter", kv._2)))
        .map(data => Ok(Json toJson MetricsResponseV1(DateTime now DateTimeZone.UTC, data.toMap)))
    )

  def getServiceMetricsV2 =
    Action.async(
      getServiceMetricsData()
        .map(data => Ok(Json toJson MetricsResponseV2(DateTime now DateTimeZone.UTC, data.toMap)))
    )

  private def getServiceMetricsData(): Future[Vector[(String, BigDecimal)]] = {
    val memoryMBean  = java.lang.management.ManagementFactory.getMemoryMXBean
    val     gcMBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans.asScala

    val statics = Vector[(String, BigDecimal)](
      "heap_used_bytes" -> memoryMBean.getHeapMemoryUsage.getUsed,
      "heap_size_bytes" -> memoryMBean.getHeapMemoryUsage.getMax,
            "gc_millis" -> gcMBeans.iterator.map(_.getCollectionTime).sum,
         "thread_count" -> Thread.activeCount()
    )

    metricsStore.getCountersSnapshot() map (counters => statics ++ counters.mapValues(BigDecimal(_)))
  }

  def getServiceConfig = Action(Ok(Json.parse(config.root.render(ConfigRenderOptions.concise))))
}
