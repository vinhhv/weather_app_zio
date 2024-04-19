package com.misterjvm.weather.http

import com.misterjvm.weather.controllers.*

object HttpAPI {
  def gatherRoutes(controllers: List[BaseController]) =
    controllers.flatMap(_.routes)

  def makeControllers = for {
    health <- HealthController.makeZIO
  } yield List(health)

  val endpointsZIO = makeControllers.map(gatherRoutes)
}
