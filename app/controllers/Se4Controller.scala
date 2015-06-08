package controllers

import play.api.mvc.{ Action, Controller }

class Se4Controller extends Controller {
  def getServiceStatus      = Action(Ok(views.html.index("Your new application is ready.")))
  def getServiceHealthcheck = Action(Ok(views.html.index("Your new application is ready.")))
  def getServiceGtg         = Action(Ok(views.html.index("Your new application is ready.")))
}
