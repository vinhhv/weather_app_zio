package com.misterjvm.weather.domain.responses

import com.misterjvm.weather.domain.data.TemperatureFeel
import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class ForecastResponse(
    // Short description of the weather condition
    weatherCondition: String,
    temperature: Int,
    // F or C
    temperatureUnit: String,
    temperatureFeel: String,
    // List of currently active alerts
    alerts: List[String]
)

object ForecastResponse {
  def apply(
      weatherCondition: String,
      temperature: Int,
      temperatureUnit: String,
      alerts: List[String]
  ): ForecastResponse = new ForecastResponse(
    weatherCondition,
    temperature,
    temperatureUnit,
    if (temperature < 50) TemperatureFeel.Cold.toString
    else if (temperature >= 50 && temperature <= 70) TemperatureFeel.Moderate.toString
    else TemperatureFeel.Hot.toString,
    alerts
  )

  given codec: JsonCodec[ForecastResponse] = DeriveJsonCodec.gen[ForecastResponse]
}
