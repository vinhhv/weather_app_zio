package com.misterjvm.weather.clients

import com.misterjvm.weather.domain.responses.ForecastResponse

trait WeatherClient {
  def getForecast(coordinates: String): ForecastResponse
}
