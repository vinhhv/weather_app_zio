package com.misterjvm.weather.services

import com.misterjvm.weather.clients.WeatherClient
import com.misterjvm.weather.domain.errors.MalformedInputException
import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.*
import zio.http.*
import zio.http.model.*
import zio.http.model.headers.HeaderNames
import zio.test.*

object WeatherServiceSpec extends ZIOSpecDefault with HeaderNames {
  val WEATHER_CONDITION = "Cloudy with a chance of snow"
  val TEMPERATURE_FEEL  = "Cold"
  val TEMPERATURE       = 45
  val TEMPERATURE_UNIT  = "F"
  val ALERTS            = List("Watch out for potential ice on road. Bring your tire chains.")

  val stubClientLayer = ZLayer {
    ZIO
      .service[Client]
      .map(client =>
        new WeatherClient(client) {
          override val headers = Headers(userAgent -> "MisterWeatherApp/1.0")

          // Should not be called
          override def getForecast(coordinates: String): Task[ForecastResponse] =
            ZIO.succeed(
              ForecastResponse(
                WEATHER_CONDITION,
                TEMPERATURE,
                TEMPERATURE_UNIT,
                ALERTS
              )
            )
        }
      )
  }

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("WeatherServiceSpec")(
      test("get forecast") {
        for {
          service  <- ZIO.service[WeatherService]
          forecast <- service.getForecast("18.431,-108.951")
        } yield assertTrue {
          forecast.weatherCondition == WEATHER_CONDITION &&
          forecast.temperatureFeel == TEMPERATURE_FEEL &&
          forecast.temperature == TEMPERATURE &&
          forecast.temperatureUnit == TEMPERATURE_UNIT &&
          forecast.alerts == ALERTS
        }
      },
      test("get forecast with malformed coordinates") {
        for {
          service  <- ZIO.service[WeatherService]
          forecast <- service.getForecast("18.43a,-108.b23").flip
        } yield assertTrue(forecast.isInstanceOf[MalformedInputException])
      }
    ).provide(WeatherServiceLive.layer, stubClientLayer, Client.default, Scope.default)
}
