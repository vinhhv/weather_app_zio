package com.misterjvm.weather.clients.weathergov

import com.misterjvm.weather.domain.data.TemperatureFeel
import zio.json.*

final case class WeatherForecastHourly(
    startTime: String,
    endTime: String,
    // Temperature unit is provided in degrees F
    temperature: Int,
    temperatureUnit: String,
    shortForecast: String
) {
  def getTemperatureFeel: String =
    if (temperature < 50) TemperatureFeel.Cold.toString
    else if (temperature >= 50 && temperature <= 70) TemperatureFeel.Moderate.toString
    else TemperatureFeel.Hot.toString

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
