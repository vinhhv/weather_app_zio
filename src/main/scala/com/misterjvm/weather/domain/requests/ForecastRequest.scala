package com.misterjvm.weather.domain.responses

import zio.json.DeriveJsonCodec
import zio.json.JsonCodec

final case class ForecastRequest(
    coordinates: String
)

object ForecastRequest {
  given codec: JsonCodec[ForecastRequest] = DeriveJsonCodec.gen[ForecastRequest]
}
