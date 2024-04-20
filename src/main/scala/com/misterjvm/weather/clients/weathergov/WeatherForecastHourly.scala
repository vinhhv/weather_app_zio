package com.misterjvm.weather.clients.weathergov

import zio.json.*

enum TemperatureFeel:
  case Cold, Moderate, Hot

final case class WeatherForecastHourly(
    startTime: String,
    endTime: String,
    // Temperature unit is provided in degrees F
    temperature: Int,
    shortForecast: String
) {
  def getTemperatureFeel: TemperatureFeel =
    if (temperature < 50) TemperatureFeel.Cold
    else if (temperature >= 50 && temperature <= 70) TemperatureFeel.Moderate
    else TemperatureFeel.Hot

}

object WeatherForecastHourly {
  given codec: JsonCodec[WeatherForecastHourly] = DeriveJsonCodec.gen[WeatherForecastHourly]
}

final case class WeatherForecasts(
    periods: List[WeatherForecastHourly]
)

object WeatherForecasts {
  given codec: JsonCodec[WeatherForecasts] = DeriveJsonCodec.gen[WeatherForecasts]
}

final case class WeatherForecastProperties(
    properties: WeatherForecasts
)

object WeatherForecastProperties {
  given codec: JsonCodec[WeatherForecastProperties] = DeriveJsonCodec.gen[WeatherForecastProperties]
}
