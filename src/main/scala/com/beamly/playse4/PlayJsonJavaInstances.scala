package com.beamly.playse4

import play.api.data.validation.ValidationError
import play.api.libs.json._

import java.net.{ URI, URL }

trait PlayJsonJavaInstances {
  implicit val urlFormat = new Format[URI] {
    def writes(uri: URI): JsValue = JsString(uri.toString)

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
}

object PlayJsonJavaInstances extends PlayJsonJavaInstances
