package com.beamly.playse4
package status

import org.joda.time.{ DateTime, Duration => JodaDuration }
import play.api.libs.json._

import java.net.URI

final case class Se4StatusResponse(
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

object Se4StatusResponse {
  implicit val jsonFormat: Format[Se4StatusResponse] = Json.format[Se4StatusResponse]
}
