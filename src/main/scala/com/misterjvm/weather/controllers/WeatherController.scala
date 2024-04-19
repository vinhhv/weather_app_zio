package com.misterjvm.weather.controllers

import com.misterjvm.weather.http.endpoints.WeatherEndpoint
import com.misterjvm.weather.services.WeatherService
import sttp.tapir.*
import sttp.tapir.server.*
import zio.*

class WeatherController private (service: WeatherService) extends BaseController with WeatherEndpoint {
  val getForecast: ServerEndpoint[Any, Task] =
    getForecastEndpoint.serverLogic { coordinates =>
      service
        .getForecast(coordinates)
        .either
    }

  override val routes: List[ServerEndpoint[Any, Task]] = List(getForecast)
}

object WeatherController {
  val makeZIO =
    for {
      service <- ZIO.service[WeatherService]
    } yield new WeatherController(service)
}
