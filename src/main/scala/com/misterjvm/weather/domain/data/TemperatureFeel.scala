package com.misterjvm.weather.domain.data

import zio.json.*

enum TemperatureFeel:
  case Cold, Moderate, Hot

object TemperatureFeel {
  given codec: JsonCodec[TemperatureFeel] = DeriveJsonCodec.gen[TemperatureFeel]
}
