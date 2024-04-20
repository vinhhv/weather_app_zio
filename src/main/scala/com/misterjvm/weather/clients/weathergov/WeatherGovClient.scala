package com.misterjvm.weather.clients.weathergov

import com.misterjvm.weather.clients.WeatherClient
import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.*
import zio.http.*
import zio.http.model.*
import zio.json.*

class WeatherGovClient private (client: Client) extends WeatherClient {
  import WeatherGovClient.*

  override def getForecast(coordinates: String): Task[ForecastResponse] =
    for {
      metadata <- getMetadata(coordinates)
      currentHourForecast <-
        getHourlyForecast(metadata.gridId, metadata.gridX, metadata.gridY)
          .someOrFail(new RuntimeException(s"Did not find any hour forecasts for $coordinates"))
      zoneId <- ZIO
        .attempt(metadata.zoneIdOpt)
        .someOrFail(new RuntimeException(s"Could not parse zone ID from url: ${metadata.forecastZone}"))
      alerts <- getAlerts(zoneId)
    } yield ForecastResponse(
      currentHourForecast.shortForecast,
      currentHourForecast.getTemperatureFeel,
      currentHourForecast.temperature,
      currentHourForecast.temperatureUnit,
      alerts.map(_.description)
    )

  override val headers = Headers(userAgent -> "MisterWeatherApp/1.0")

  def getMetadata(coordinates: String): Task[WeatherMetadata] = {
    makeRequest[WeatherMetadataProperties, WeatherMetadata](
      "fetch metadata",
      getMetadataUrl(coordinates),
      client
    )(_.properties)
  }

  def getHourlyForecast(gridId: String, gridX: Int, gridY: Int): Task[Option[WeatherForecastHourly]] =
    makeRequest[WeatherForecastProperties, Option[WeatherForecastHourly]](
      "fetch hourly forecasts",
      getHourlyForecastUrl(gridId, gridX, gridY),
      client
    )(
      _.properties.periods.headOption
    ) // Grabs the first hourly forecast from the response as we only need the current hourly forecast

  def getAlerts(zoneId: String): Task[List[WeatherAlert]] =
    makeRequest[WeatherAlertFeatures, List[WeatherAlert]](
      "fetch weather alerts",
      getAlertsUrl(zoneId),
      client
    )(_.features.map(_.properties))
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
    metadata         <- weatherGovClient.getMetadata("39.44,-105.86")
    hourlyForecast <- weatherGovClient.getHourlyForecast(
      metadata.gridId,
      metadata.gridX,
      metadata.gridY
    )
    zoneId <- ZIO
      .attempt(metadata.zoneIdOpt)
      .someOrFail(new RuntimeException(s"Failed to parse zone ID from ${metadata.forecastZone}"))
    alerts <- weatherGovClient.getAlerts(zoneId)
    _      <- Console.printLine("\n\n\nMetadata")
    _      <- Console.printLine(metadata)
    _      <- Console.printLine("\n\nHour Forecast")
    _      <- Console.printLine(hourlyForecast)
    _      <- Console.printLine("\n\nAlerts")
    _      <- Console.printLine(alerts)
    _      <- Console.printLine("\n\n\n")
  } yield ()

  override def run: ZIO[Any & (ZIOAppArgs & Scope), Any, Any] =
    program.provide(WeatherGovClient.layer, Client.default, Scope.default)
}
