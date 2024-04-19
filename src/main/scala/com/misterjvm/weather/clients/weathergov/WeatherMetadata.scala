package com.misterjvm.weather.clients.weathergov

import zio.json.*

final case class WeatherMetadata(
    gridX: Int,
    gridY: Int
)
object WeatherMetadata {
  given codec: JsonCodec[WeatherMetadata] = DeriveJsonCodec.gen[WeatherMetadata]
}

final case class WeatherMetadataProperties(
    properties: WeatherMetadata
)
object WeatherMetadataProperties {
  given codec: JsonCodec[WeatherMetadataProperties] = DeriveJsonCodec.gen[WeatherMetadataProperties]
}
