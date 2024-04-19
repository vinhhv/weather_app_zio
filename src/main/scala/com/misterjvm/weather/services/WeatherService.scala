package com.misterjvm.weather.services

import com.misterjvm.weather.domain.constants
import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.*

trait WeatherService {
  def getForecast(coordinates: String): ForecastResponse
}

class WeatherServiceLive private (weatherClient: WeatherClient) extends WeatherService {
  override def getForecast(coordinates: String): Task[ForecastResponse] =
    for {
      coordinatesVerified <- WeatherService.verifyCoordinates(coordinates)
      weatherResponse     <- weatherClient.getForecast(coordinatesVerified)
    } yield ForecastResponse("", "", Some(""))
}

object WeatherService {
  def verifyCoordinates(coordinates: String): Task[String] =
    if (!coordinates.matches(constants.coordinatesRegex))
      ZIO.fail(
        new IllegalArgumentException(
          "Coordinates are not properly formatted. Please follow this example: '39.7456,-97.0892'"
        )
      )
    else
      ZIO.succeed(coordinates)
}
