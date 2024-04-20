package com.misterjvm.weather.clients.weathergov

import zio.json.*

// Assumption: this is enough to cover a "standard" alert
// Can easily add any other variables here and will automatically parse
final case class WeatherAlert(
    // Description of alert
    description: String,
    // Timestamp of effective time/date
    effective: String,
    // Timestamp of effective end time/date
    ends: String
)

object WeatherAlert {
  given codec: JsonCodec[WeatherAlert] = DeriveJsonCodec.gen[WeatherAlert]
}

final case class WeatherAlertProperties(
    properties: WeatherAlert
)

object WeatherAlertProperties {
  given codec: JsonCodec[WeatherAlertProperties] = DeriveJsonCodec.gen[WeatherAlertProperties]
}

final case class WeatherAlertFeatures(
    features: List[WeatherAlertProperties]
)

object WeatherAlertFeatures {
  given codec: JsonCodec[WeatherAlertFeatures] = DeriveJsonCodec.gen[WeatherAlertFeatures]
}
