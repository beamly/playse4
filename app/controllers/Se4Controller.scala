package controllers

import org.joda.time.{ Duration => JodaDuration, Interval, DateTime, DateTimeZone }
import play.api.libs.json.{ Format, JsResult, JsValue, Json }
import play.api.mvc.{ Action, AnyContent, Controller }

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import java.net.JarURLConnection
import java.util.concurrent.TimeUnit

// TODO: Restrict to return only json
class Se4Controller extends Controller {
  def getServiceStatus: Action[AnyContent] = {

    // TODO: inject class?
    // TODO: handle IO exception
    val clazz = getClass

    val pathName = clazz.getName.replaceAll("\\.", "/")
    val attributesOpt =
      for {
        resourceUrl   <- Option(clazz getResource s"/$pathName.class")
        urlConnection = resourceUrl.openConnection() if urlConnection.isInstanceOf[JarURLConnection]
        jarConnection = urlConnection.asInstanceOf[JarURLConnection]
        manifest     <- Option(jarConnection.getManifest)
      } yield
      manifest.getMainAttributes.asScala.map(kv => kv._1.toString -> kv._2.toString).toMap

    val attributes = attributesOpt getOrElse Map.empty

    lazy val version: String =
      (attributes
        get "Version"
        map (ver =>
          attributes
            get "Build-Number"
            filter (_.nonEmpty)
            map (bn => ver.replace("SNAPSHOT", bn))
            getOrElse ver
          )
        getOrElse "n/a"
      )

    val osMBean = java.lang.management.ManagementFactory.getOperatingSystemMXBean
    val runTimeMBean = java.lang.management.ManagementFactory.getRuntimeMXBean

    val hostName    = java.net.InetAddress.getLocalHost.getHostName
    val hostAddress = java.net.InetAddress.getLocalHost.getHostAddress

    val currentDateTime = DateTime now DateTimeZone.UTC
    val upSince = new DateTime(runTimeMBean.getStartTime, DateTimeZone.UTC)

    val serviceStatus = ServiceStatus(
      group_id         = attributes.get("Group-Id") getOrElse "n/a",
      artifact_id      = attributes.get("Artifact-Id") getOrElse "n/a",
      version          = version,
      git_sha1         = attributes.get("Git-SHA1") getOrElse "n/a",

      built_by         = attributes.get("Built-By") getOrElse "n/a",
      build_number     = attributes.get("Build-Number") getOrElse "n/a",
      build_machine    = attributes.get("Build-Machine") getOrElse "n/a",
      built_when       = attributes.get("Built-When") getOrElse "n/a",
      compiler_version = attributes.get("Build-Jdk") getOrElse "n/a",

      machine_name     = s"$hostName ($hostAddress)",
      current_time     = currentDateTime,
      up_since         = upSince,
      up_duration      = new Interval(upSince, currentDateTime).toDuration,

      os_arch          = osMBean.getArch,
      os_numprocessors = Option(osMBean.getAvailableProcessors.toString),
      os_name          = osMBean.getName,
      os_version       = osMBean.getVersion,
      os_avgload       = Option(osMBean.getSystemLoadAverage.toString),

      vm_name          = runTimeMBean.getVmName,
      vm_vendor        = runTimeMBean.getVmVendor,
      vm_version       = runTimeMBean.getVmVersion,
      runbook_url      = "" //new URI("http://google.com")
    )

    Action(Ok(Json toJson serviceStatus))
  }
  def getServiceHealthcheck = Action(Ok(views.html.index("Your new application is ready.")))
  def getServiceGtg         = Action(Ok(views.html.index("Your new application is ready.")))
}

// TODO: Ask Glen about below being of type float instead of string
final case class ServiceStatus(
  group_id         : String,
  artifact_id      : String,
  version          : String,
  git_sha1         : String,

  built_by         : String,
  build_number     : String,
  build_machine    : String,
  built_when       : String,
  compiler_version : String,

  machine_name     : String,
  current_time     : DateTime,
  up_since         : DateTime,
  up_duration      : JodaDuration,

  os_arch          : String,
  os_numprocessors : Option[String],
  os_name          : String,
  os_version       : String,
  os_avgload       : Option[String],

  vm_name          : String,
  vm_vendor        : String,
  vm_version       : String,
  runbook_url      : String // URI
)
object ServiceStatus {
  implicit val dateTimeFormat = ??? // TODO

  implicit val durationFormat = new Format[JodaDuration] {
    def writes(o: JodaDuration): JsValue =
      Json toJson Duration(o.getMillis, TimeUnit.MILLISECONDS).toString

    def reads(json: JsValue): JsResult[JodaDuration] =
      json.validate[String] map (Duration(_)) map (_.toMillis) map JodaDuration.millis
  }
  implicit val jsonFormat: Format[ServiceStatus] = Json.format[ServiceStatus]
}
