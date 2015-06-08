package controllers

import org.joda.time.{ DateTime, DateTimeZone, Duration => JodaDuration }
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.mvc.{ Action, AnyContent, Controller }

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import java.net.{URI, URL, JarURLConnection}
import java.util.concurrent.TimeUnit

// TODO: Restrict to return only json
// TODO: Consider a saner up_duration output ("16890642 milliseconds" is ridiculous)
class Se4Controller extends Controller {
  def getServiceStatus: Action[AnyContent] = {
    // TODO: Make `clazz` injected at construction, & find a better name
    val clazz = getClass

    val pathName = clazz.getName.replaceAll("\\.", "/")
    val attributes = (
      (for {
        resourceUrl   <- Option(clazz getResource s"/$pathName.class")
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

    val serviceStatus =
      ServiceStatus(
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
  os_numprocessors : String,
  os_name          : String,
  os_version       : String,
  os_avgload       : String,

  vm_name          : String,
  vm_vendor        : String,
  vm_version       : String,
  runbook_url      : URI
)
object ServiceStatus {
  implicit val dateTimeFormat =
    Format(
      Reads  jodaDateReads  "yyyy-MM-dd'T'HH:mm:ss.SSSZZ",
      Writes jodaDateWrites "yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
    )

  implicit val durationFormat = new Format[JodaDuration] {
    def writes(d: JodaDuration): JsValue =
      Json toJson Duration(d.getMillis, TimeUnit.MILLISECONDS).toString

    def reads(json: JsValue): JsResult[JodaDuration] =
      json.validate[String] map (Duration(_)) map (_.toMillis) map JodaDuration.millis
  }

  implicit val urlFormat = new Format[URI] {
    def writes(uri: URI): JsValue = Json toJson uri.toString

    def reads(json: JsValue): JsResult[URI] =
      json match {
        case JsString(s) => parseURI(s) match {
          case Some(uri) => JsSuccess(uri)
          case None      => JsError(ValidationError("error.expected.url.format", s))
        }
        case _           => JsError(ValidationError("error.expected.url", json.toString))
      }

    private def parseURI(s: String) = scala.util.control.Exception.allCatch[URI] opt new URL(s).toURI
  }

  implicit val jsonFormat: Format[ServiceStatus] = Json.format[ServiceStatus]
}
