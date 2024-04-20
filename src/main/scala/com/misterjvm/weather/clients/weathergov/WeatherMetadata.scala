package com.misterjvm.weather.clients.weathergov

import zio.json.*

final case class WeatherMetadata(
    gridId: String,
    gridX: Int,
    gridY: Int,
    forecastZone: String
) {
  val zoneIdOpt: Option[String] = {
    val pattern = ".*/([^/]+)$".r
    forecastZone match {
      case pattern(zone) => Some(zone)
      case _             => None
    }
  }
}

object WeatherMetadata {
  given codec: JsonCodec[WeatherMetadata] = DeriveJsonCodec.gen[WeatherMetadata]
}

final case class WeatherMetadataProperties(
    properties: WeatherMetadata
)
object WeatherMetadataProperties {
  given codec: JsonCodec[WeatherMetadataProperties] = DeriveJsonCodec.gen[WeatherMetadataProperties]
}
