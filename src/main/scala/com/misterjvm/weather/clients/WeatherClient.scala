package com.misterjvm.weather.clients

import com.misterjvm.weather.domain.responses.ForecastResponse
import zio.http.*
import zio.http.model.*
import zio.http.model.headers.HeaderNames
import zio.Task

trait WeatherClient extends HeaderNames {
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

  def getRequest(url: URL, headers: Headers): Request =
    Request(
      body = Body.empty,
      headers = headers,
      method = Method.GET,
      url = url,
      version = Version.`HTTP/1.1`,
      Option.empty
    )
}
