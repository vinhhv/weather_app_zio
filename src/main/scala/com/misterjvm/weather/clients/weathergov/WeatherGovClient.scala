package com.misterjvm.weather.clients.weathergov

import com.misterjvm.weather.clients.WeatherClient
import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.*
import zio.http.*
import zio.http.model.*
import zio.json.*

class WeatherGovClient private (client: Client) extends WeatherClient {
  override def getForecast(coordinates: String): Task[ForecastResponse] = ???

  import WeatherGovClient.*

  def getMetadata(coordinates: String): Task[WeatherMetadataProperties] = {
    for {
      url <- ZIO.fromEither(URL.fromString(getMetadataUrl(coordinates)))
      response <- client.request(
        getRequest(
          url = url,
          headers = Headers(
            userAgent -> "MisterWeatherApp/1.0"
          )
        )
      )
      metadata <-
        response.body.asString.map { json =>
          json.fromJson[WeatherMetadataProperties] match {
            case Left(message) =>
              Left(new RuntimeException(s"Failed to parse weather metadata '$coordinates': $message"))
            case Right(parsed) => Right(parsed)
          }
        }.absolve
    } yield metadata
  }

  // Grabs the first hourly forecast from the response as we only need the current hourly forecast
  def getHourlyForecast(gridId: String, gridX: Int, gridY: Int): Task[WeatherForecastHourly] =
    for {
      url <- ZIO.fromEither(URL.fromString(getHourlyForecastUrl(gridId, gridX, gridY)))
      response <- client.request(
        getRequest(
          url = url,
          headers = Headers(
            userAgent -> "MisterWeatherApp/1.0"
          )
        )
      )
      currentHourlyForecast <-
        response.body.asString
          .map { json =>
            (json.fromJson[WeatherForecastProperties] match {
              case Left(message) =>
                Left(new RuntimeException(s"Failed to parse hourly forecasts: $message"))
              case Right(forecasts) => Right(forecasts)
            })
              .map(_.properties.periods.headOption)
          }
          .absolve
          .someOrFail(new RuntimeException("No forecasts available for the provided coordinates"))
    } yield currentHourlyForecast

  def getAlerts(zoneId: String): Task[List[WeatherAlert]] =
    for {
      url <- ZIO.fromEither(URL.fromString(getAlertsUrl(zoneId)))
      response <- client.request(
        getRequest(
          url = url,
          headers = Headers(
            userAgent -> "MisterWeatherApp/1.0"
          )
        )
      )
      alerts <-
        response.body.asString.map { json =>
          (json.fromJson[WeatherAlertFeatures] match {
            case Left(message) =>
              Left(new RuntimeException(s"Failed to parse alerts: $message"))
            case Right(alerts) => Right(alerts)
          })
            .map(_.features)
        }.absolve
    } yield alerts
}

object WeatherGovClient {
  val baseUrl                             = "https://api.weather.gov"
  def getMetadataUrl(coordinates: String) = s"$baseUrl/points/$coordinates"
  def getHourlyForecastUrl(gridId: String, gridX: Int, gridY: Int) =
    s"$baseUrl/gridpoints/$gridId/$gridX,$gridY/forecast/hourly"
  def getAlertsUrl(zoneId: String) = s"$baseUrl/alerts/active/zone/$zoneId"

  val layer = ZLayer {
    ZIO.service[Client].map(client => new WeatherGovClient(client))
  }
}

object WeatherGovDemo extends ZIOAppDefault {
  val program = for {
    weatherGovClient <- ZIO.service[WeatherGovClient]
    metadata         <- weatherGovClient.getMetadata("39.7456,-97.0892").map(_.properties)
    hourlyForecast <- weatherGovClient.getHourlyForecast(
      metadata.gridId,
      metadata.gridX,
      metadata.gridY
    )
    _ <- Console.printLine(metadata)
  } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    program.provide(WeatherGovClient.layer, Client.default, Scope.default)
}
