package com.misterjvm.weather.clients.weathergov

import zio.json.*

final case class WeatherError(
    detail: String
)

object WeatherError {
  given codec: JsonCodec[WeatherError] = DeriveJsonCodec.gen[WeatherError]
}
