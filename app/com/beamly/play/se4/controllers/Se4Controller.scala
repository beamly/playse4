package com.beamly.play.se4.controllers

import akka.actor.Cancellable
import com.beamly.play.se4.{JarManifest, ServiceStatusData}
import com.beamly.play.se4.healthcheck.{HealthCheck, TestPassed}
import com.beamly.play.se4.healthchecks.HealthcheckResponse
import org.joda.time.{DateTime, DateTimeZone, Duration => JodaDuration}
import play.Logger
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import java.net.URI

// TODO: Restrict to return only json
// TODO: Consider/trial moving to com.beamly.play.se4
class Se4Controller(healthchecks: Iterable[HealthCheck]) extends Controller {
  def getServiceStatus: Action[AnyContent] = {
    // TODO: Make `clazz` injected at construction, & find a better name - anyServiceClass
    val clazz = getClass

    val manifest = JarManifest fromClass clazz getOrElse Map.empty withDefault(_ => "n/a")

    val      osMBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean
    val runTimeMBean = java.lang.management.ManagementFactory.getRuntimeMXBean

    val hostName    = java.net.InetAddress.getLocalHost.getHostName
    val hostAddress = java.net.InetAddress.getLocalHost.getHostAddress

    val currentDateTime = DateTime now DateTimeZone.UTC
    val upSince = new DateTime(runTimeMBean.getStartTime, DateTimeZone.UTC)

    val serviceStatusData =
      ServiceStatusData(
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
        runbook_url      = new URI("http://google.com")
    )

    Action(Ok(Json toJson serviceStatusData))
  }

  def getServiceGtg = Action async {
    healthchecks.foldLeft(Future successful true) { (soFar, test) =>
      test.gtgPassed flatMap (gtgPassed => soFar map (_ && gtgPassed))
    } map {
      case true  => Ok("\"OK\"")
      case false => InternalServerError("\"FAILED\"")
    }
  }

  def getServiceHealthcheck = Action async {
    Future.traverse(healthchecks)(_.latestResult) map { testResults =>
      val healthCheck = HealthcheckResponse.factory(DateTime.now(), "0 seconds", testResults)
      Ok(Json.toJson(healthCheck))
    }
  }

  private var scheduledTests = Iterable.empty[Cancellable]

  def scheduleHealthChecks()(implicit app: play.api.Application) {
    scheduledTests = healthchecks map { healthCheck =>
      Akka.system.scheduler.schedule(Duration.Zero, healthCheck.testInterval) {
        healthCheck.invokeTest()(Akka.system)
        healthCheck.latestResult map { result =>
          if (result.status == TestPassed) {
            Logger.debug( "HealthCheck [{}] {}", result.name, result.status)
          } else {
            Logger.info( "HealthCheck [{}] {}", result.name,  result.status)
          }
        }
      }
    }
  }

  def unscheduleHealthChecks() {
    scheduledTests foreach (_.cancel())
    scheduledTests = Iterable.empty
  }

  def getServiceMetrics     = Action(Ok("TODO"))
  def getServiceTwoMetrics  = Action(Ok("TODO"))





  def getServiceConfig      = Action(Ok("TODO"))
}
