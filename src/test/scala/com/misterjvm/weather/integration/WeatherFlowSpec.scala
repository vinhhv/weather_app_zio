package com.misterjvm.weather.integration

import com.misterjvm.weather.clients.*
import com.misterjvm.weather.clients.weathergov.*
import com.misterjvm.weather.controllers.*
import com.misterjvm.weather.domain.responses.*
import com.misterjvm.weather.services.*
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{SttpBackend, _}
import sttp.model.Method
import sttp.monad.MonadError
import sttp.tapir.generic.auto.*
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.*
import zio.http.*
import zio.http.model.{Header, Headers}
import zio.http.model.headers.HeaderNames
import zio.json.*
import zio.test.*
import zio.test.Assertion.*

object WeatherFlowSpec extends ZIOSpecDefault with HeaderNames {
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO =
    for {
      controller <- WeatherController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointsRunLogic(controller.routes)
          .backend()
      )
    } yield backendStub

  extension (backend: SttpBackend[Task, Nothing]) {
    def sendRequest[R: JsonCodec](
        method: Method,
        path: String
    ): Task[Either[String, Option[R]]] =
      basicRequest
        .method(method, uri"$path")
        .header("User-Agent", "MisterWeatherApp/1.0")
        .send(backend)
        .map(_.body)
        .map(_.map(_.fromJson[R].toOption))

    def get[R: JsonCodec](path: String): Task[Either[String, Option[R]]] =
      sendRequest(Method.GET, path)
  }

  val GOOD_COORDINATES          = "39.44,-105.868"
  val BAD_COORDINATES           = "abc,123"
  val OUT_OF_BOUNDS_COORDINATES = "47.123,179.999"

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("WeatherFlow")(
      test("get forecast") {
        for {
          backendStub   <- backendStubZIO
          maybeResponse <- backendStub.get[ForecastResponse](s"/forecast/$GOOD_COORDINATES")
        } yield assert(maybeResponse)(isRight)
      },
      test("get forecast malformed coordinates") {

        for {
          backendStub   <- backendStubZIO
          maybeResponse <- backendStub.get[ForecastResponse](s"/forecast/$BAD_COORDINATES")
        } yield assert(maybeResponse)(
          isLeft(
            equalTo(
              "Coordinates are not properly formatted or are out of range. Please follow this example: '39.7456,-97.0892'"
            )
          )
        )
      },
      test("get forecast coordinates out of range") {
        for {
          backendStub   <- backendStubZIO
          maybeResponse <- backendStub.get[ForecastResponse](s"/forecast/$OUT_OF_BOUNDS_COORDINATES")
        } yield assert(maybeResponse)(isLeft(equalTo("Unable to provide data for requested point 47.123,179.999")))
      }
    ).provide(
      // services
      WeatherServiceLive.layer,
      // clients
      WeatherGovClient.layer,
      // HTTP ZClient
      Client.default,
      Scope.default
    )
}
