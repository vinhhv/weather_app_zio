package com.misterjvm.weather.http

import com.misterjvm.weather.controllers.*

object HttpAPI {
  def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  def makeControllers = for {
    health  <- HealthController.makeZIO
    weather <- WeatherController.makeZIO
  } yield List(health, weather)

  val endpointsZIO = makeControllers.map(gatherRoutes)
}
