package com.misterjvm.weather

import com.misterjvm.weather.configs.*
import com.misterjvm.weather.clients.weathergov.*
import com.misterjvm.weather.http.*
import com.misterjvm.weather.services.*
import sttp.tapir.*
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.ziohttp.{ZioHttpInterpreter, ZioHttpServerOptions}
import zio.*
import zio.http.*

import java.net.InetSocketAddress

object Application extends ZIOAppDefault {
  val configuredServer =
    Configs.makeLayer[HttpConfig]("misterjvm.http") >>>
      ZLayer(
        ZIO.service[HttpConfig].map(config => ServerConfig.default.copy(address = InetSocketAddress(config.port)))
      ) >>> Server.live

  def startServer = for {
    endpoints <- HttpAPI.endpointsZIO
    _ <- Server.serve(
      ZioHttpInterpreter(
        ZioHttpServerOptions.default.appendInterceptor(
          CORSInterceptor.default
        )
      ).toHttp(endpoints)
    )
  } yield ()

  def program = for {
    _ <- ZIO.log("Starting weather app...")
    _ <- startServer
  } yield ()

  override def run = program.provide(
    configuredServer,
    // services
    WeatherServiceLive.layer,
    // clients
    WeatherGovClient.layer,
    // HTTP ZClient
    Client.default,
    Scope.default
  )
}
