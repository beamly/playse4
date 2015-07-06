package com.beamly.playse4

import com.beamly.playse4.controllers.Se4Controller
import play.api.ApplicationLoader.Context
import play.api.{ ApplicationLoader, BuiltInComponentsFromContext }
import router.Routes

import java.net.URI

class ScratchComponents(context: Context) extends BuiltInComponentsFromContext(context) with PlaySe4Components {
  def aServiceClass = classOf[Se4Controller]
  def runbookUrl = new RunbookUrl(new URI("https://google.com"))
  def healthchecks = Nil

  lazy val router = new Routes(httpErrorHandler, se4Controller)
}

class ScratchApplicationLoader extends ApplicationLoader {
  def load(context: Context) = new ScratchComponents(context).application
}
