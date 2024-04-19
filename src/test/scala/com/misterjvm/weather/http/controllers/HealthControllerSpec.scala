package com.misterjvm.weather.http.controllers

import com.misterjvm.weather.controllers.HealthController
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.model.StatusCode
import sttp.monad.MonadError
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.json.*
import zio.test.*
import zio.*

object HealthControllerSpec extends ZIOSpecDefault {
  private given zioME: MonadError[Task] = new RIOMonadError[Any]

  private def backendStubZIO(endpointF: HealthController => ServerEndpoint[Any, Task]) =
    for {
      controller <- HealthController.makeZIO
      backendStub <- ZIO.succeed(
        TapirStubInterpreter(SttpBackendStub(MonadError[Task]))
          .whenServerEndpointRunLogic(endpointF(controller))
          .backend()
      )
    } yield backendStub

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("HealthControllerSpec")(
      test("Check health") {
        for {
          backendStub <- backendStubZIO(_.health)
          response <- basicRequest
            .get(uri"health")
            .send(backendStub)
        } yield assertTrue(response.code == StatusCode.Ok)
      }
    )
}
