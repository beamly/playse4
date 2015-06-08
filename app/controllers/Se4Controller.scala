package controllers

import org.joda.time.{ Duration, DateTime }
import play.api.mvc.{ Action, Controller }

import java.net.URI

class Se4Controller extends Controller {
  def getServiceStatus      = Action(Ok(views.html.index("Your new application is ready.")))
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
  up_duration      : Duration,

  os_arch          : String,
  os_numprocessors : Option[String],
  os_name          : String,
  os_version       : String,
  os_avgload       : Option[String],

  vm_name          : String,
  vm_vendor        : String,
  vm_version       : String,
  runbook_url      : URI
)
