package com.misterjvm.weather.domain.responses

import com.misterjvm.weather.domain.data.TemperatureFeel
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class ForecastResponse(
    // Short description of the weather condition
    weatherCondition: String,
    // How the temperature feels, check TemperatureFeel for possible cases
    temperatureFeel: String,
    temperature: Int,
    // F or C
    temperatureUnit: String,
    // List of currently active alerts
    alerts: List[String]
)

object ForecastResponse {
  given codec: JsonCodec[ForecastResponse] = DeriveJsonCodec.gen[ForecastResponse]
}
