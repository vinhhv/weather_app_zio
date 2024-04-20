package com.misterjvm.weather.clients

import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.http.*
import zio.http.model.*
import zio.http.model.headers.HeaderNames
import zio.json.*
import zio.*

trait WeatherClient extends HeaderNames {
  val headers: Headers

  def getForecast(coordinates: String): Task[ForecastResponse]

  def buildRequest(url: URL, headers: Headers, method: Method): Request =
    Request(
      body = Body.empty,
      headers = headers,
      method = method,
      url = url,
      version = Version.`HTTP/1.1`,
      Option.empty
    )

  def buildGetRequest(url: URL, headers: Headers): Request =
    buildRequest(url, headers, Method.GET)

  /**
   * Builds and sends the request to the desired API while allowing the caller
   * to extract deeper, nested values in the response using `extractF`.
   */
  def makeRequest[A: JsonCodec, B](
      description: String,
      urlString: String,
      client: Client
  )(extractF: A => B): Task[B] =
    for {
      url <- ZIO.fromEither(URL.fromString(urlString))
      response <- client.request(
        buildGetRequest(
          url = url,
          headers = headers
        )
      )
      _ <- response.status match {
        case Status.Ok => ZIO.unit
        case status =>
          ZIO.fail(
            new java.lang.RuntimeException(
              s"Failed to $description with status ${status.code}: ${response.body.asString}"
            )
          )
      }
      result <-
        response.body.asString.map { json =>
          (json.fromJson[A] match {
            case Left(message) =>
              Left(new RuntimeException(s"Failed to parse response from JSON: $message"))
            case Right(result) => Right(result)
          }).map(extractF(_))
        }.absolve
    } yield result
}
