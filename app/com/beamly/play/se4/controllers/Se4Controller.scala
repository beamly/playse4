package com.beamly.play.se4.controllers

import com.beamly.play.se4.{ JarManifest, ServiceStatusData }
import org.joda.time.{ DateTime, DateTimeZone, Duration => JodaDuration }
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent, Controller }

import java.net.URI

// TODO: Restrict to return only json
// TODO: Consider/trial moving to com.beamly.play.se4
class Se4Controller extends Controller {
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





  def getServiceGtg         = Action(Ok("TODO"))
  def getServiceHealthcheck = {
    Action(Ok("TODO"))
  }





  def getServiceMetrics     = Action(Ok("TODO"))
  def getServiceTwoMetrics  = Action(Ok("TODO"))





  def getServiceConfig      = Action(Ok("TODO"))
}
