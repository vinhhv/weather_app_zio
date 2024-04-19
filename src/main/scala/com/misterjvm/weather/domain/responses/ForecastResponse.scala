package com.misterjvm.weather.domain.responses

import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class ForecastResponse(
    weatherCondition: String,
    temperatureFeel: String,
    alert: Option[String]
)

object ForecastResponse {
  given codec: JsonCodec[ForecastResponse] = DeriveJsonCodec.gen[ForecastResponse]
}
