package com.misterjvm.weather.clients

import com.misterjvm.weather.domain.errors.UpstreamServiceException
import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.http.*
import zio.http.model.*
import zio.json.*
import zio.*

// Generic client for weather APIs
abstract class WeatherClient(client: Client) {

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
      urlString: String
  )(extractF: A => B)(extractErrorF: E => String): Task[B] =
    for {
      url <- ZIO.fromEither(URL.fromString(urlString))
      response <- client.request(
        buildGetRequest(
          url = url,
          headers = headers
        )
      )
      _      <- parseErrorIfExists[E](response, extractErrorF)
      result <- parseResponse(response, extractF)
    } yield result

  // Attempts to parse an error message if the upstream service returns anything but a 200 OK.
  // `extractErrorF` is passed in by the caller who knows the structure of the error E.
  private def parseErrorIfExists[E: JsonCodec](response: Response, extractErrorF: E => String): Task[Unit] =
    response.status match {
      case Status.Ok => ZIO.unit
      case status    =>
        // Attempt to parse error message, if error came from API service
        response.body.asString.flatMap { json =>
          (json.fromJson[E] match {
            case Left(message) =>
              // Error occurred elsewhere or broken error response
              ZIO.fail(new RuntimeException(s"Failed with unknown error: $message"))
            case Right(error) => ZIO.fail(new UpstreamServiceException(extractErrorF(error)))
          })
        }
    }

  // Attempts to parse the response from the upstream service into the provided structure A.
  // `extractF` flattens the JSON response into the desired structure for further processing.
  private def parseResponse[A: JsonCodec, B](response: Response, extractF: A => B): Task[B] =
    response.body.asString.map { json =>
      (json.fromJson[A] match {
        case Left(message) =>
          Left(new RuntimeException(s"Failed to parse response from JSON: $message"))
        case Right(result) => Right(result)
      }).map(extractF(_))
    }.absolve

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
