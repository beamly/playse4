package controllers

import com.beamly.play.se4.ServiceStatusData
import org.joda.time.{ DateTime, DateTimeZone, Duration => JodaDuration }
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent, Controller }

import scala.collection.JavaConverters._
import java.net.{ JarURLConnection, URI }

// TODO: Restrict to return only json
// TODO: Consider/trial moving to com.beamly.play.se4
class Se4Controller extends Controller {
  def getServiceStatus: Action[AnyContent] = {
    // TODO: Make `clazz` injected at construction, & find a better name - anyServiceClass
    val clazz = getClass

    val pathName = clazz.getName.replaceAll("\\.", "/")
    val attributes = (
      (for {
        resourceUrl  <- Option(clazz getResource s"/$pathName.class")
        urlConnection = resourceUrl.openConnection() if urlConnection.isInstanceOf[JarURLConnection]
        jarConnection = urlConnection.asInstanceOf[JarURLConnection]
        manifest     <- Option(jarConnection.getManifest)
      } yield
        manifest.getMainAttributes.asScala.map(kv => kv._1.toString -> kv._2.toString).toMap)
      getOrElse Map.empty
      withDefault(_ => "n/a")
    )

    val      osMBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean
    val runTimeMBean = java.lang.management.ManagementFactory.getRuntimeMXBean

    val hostName    = java.net.InetAddress.getLocalHost.getHostName
    val hostAddress = java.net.InetAddress.getLocalHost.getHostAddress

    val currentDateTime = DateTime now DateTimeZone.UTC
    val upSince = new DateTime(runTimeMBean.getStartTime, DateTimeZone.UTC)

    val serviceStatusData =
      ServiceStatusData(
        group_id         = attributes("Group-Id"),
        artifact_id      = attributes("Artifact-Id"),
        version          = attributes("Version"),
        git_sha1         = attributes("Git-SHA1"),

        built_by         = attributes("Built-By"),
        build_number     = attributes("Build-Number"),
        build_machine    = attributes("Build-Machine"),
        built_when       = attributes("Built-When"),
        compiler_version = attributes("Build-Jdk"),

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
  def getServiceHealthcheck = Action(Ok("TODO"))





  def getServiceMetrics     = Action(Ok("TODO"))
  def getServiceTwoMetrics  = Action(Ok("TODO"))





  def getServiceConfig      = Action(Ok("TODO"))
}
