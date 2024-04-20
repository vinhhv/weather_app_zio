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
      url <- ZIO.fromEither(URL.fromString(s"$getMetadataUrl/$coordinates"))
      response <- client.request(
        getRequest(
          url = url,
          headers = Headers(
            userAgent -> "MisterWeatherApp/1.0"
          )
        )
      )
      metadata <-
        response.body.asString.map { s =>
          s.fromJson[WeatherMetadataProperties] match {
            case Left(message) =>
              Left(new RuntimeException(s"Failed to parse weather metadata for ${url.path}: $message"))
            case Right(parsed) => Right(parsed)
          }
        }.absolve
    } yield metadata
  }

  def getHourlyForecast(gridId: String, gridX: Int, gridY: Int): Task[WeatherForecast]
}

object WeatherGovClient {
  val baseUrl                             = "https://api.weather.gov"
  def getMetadataUrl(coordinates: String) = s"$baseUrl/points/$coordinates"
  def getHourlyForecastUrl(gridId: String, gridX: Int, gridY: Int) =
    s"$baseUrl/gridpoints/$gridId/$gridX,$gridY/forecast/hourly"

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
