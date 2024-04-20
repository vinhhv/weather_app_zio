package com.misterjvm.weather.clients

import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.http.*
import zio.http.model.*
import zio.http.model.headers.HeaderNames
import zio.json.*
import zio.*

// Generic client for weather APIs. E represents the error model returned by the API.
trait WeatherClient extends HeaderNames {

  import WeatherClient.*

  val headers: Headers

  def getForecast(coordinates: String): Task[ForecastResponse]

  /**
   * Builds and sends the request to the desired API while allowing the caller
   * to extract deeper, nested values in the response using `extractF`.
   * 
   * A represents the initial JSON model that is derived.
   * B represents the final "flattened" model that only contains useful information.
   * E represents the error model.
   */
  def makeRequest[A: JsonCodec, B, E: JsonCodec](
      description: String,
      urlString: String,
      client: Client
  )(extractF: A => B)(extractErrorF: E => String): Task[B] =
    for {
      url <- ZIO.fromEither(URL.fromString(urlString))
      response <- client.request(
        buildGetRequest(
          url = url,
          headers = headers
        )
      )
      // Parse error if status not OK
      _ <- response.status match {
        case Status.Ok => ZIO.unit
        case status    =>
          // Attempt to parse error message, if error came from API service
          response.body.asString.flatMap { json =>
            (json.fromJson[E] match {
              case Left(message) =>
                // Error occurred elsewhere or broken error response
                ZIO.fail(new RuntimeException(s"Failed with unknown error: $message"))
              case Right(error) => ZIO.fail(new RuntimeException(extractErrorF(error)))
            })
          }
      }
      // Parse result
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

object WeatherClient {
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
}
