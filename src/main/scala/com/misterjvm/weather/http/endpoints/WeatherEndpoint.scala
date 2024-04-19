package com.misterjvm.weather.http.endpoints

import com.misterjvm.weather.domain.responses.ForecastResponse
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.zio.*
import zio.*

trait WeatherEndpoint extends BaseEndpoint {
  val getForecastEndpoint =
    baseEndpoint
      .tag("Weather")
      .name("getForecast")
      .description("Get forecast for given geographic coordinates (latitude/longitude)")
      .in("forecast" / path[String]("coordinates"))
      .get
      .out(jsonBody[ForecastResponse])
}
