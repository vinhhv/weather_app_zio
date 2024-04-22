package com.misterjvm.weather.domain

object constants {

  /**
   * Regex is based on the following rules:
   *
   * Latitude measures how far north or south a location is from the equator.
   * The equator is at 0 degrees latitude. The North Pole is at +90 degrees,
   * and the South Pole is at -90 degrees.
   *
   * Longitude measures how far east or west a location is from the prime meridian,
   * which runs through Greenwich, England. Longitude values increase to the east
   * of the prime meridian and decrease to the west. Range is -180 to +180 degrees.
   */
  //
  // Longitude
  val coordinatesRegex =
    """^[-]?([1-8]?\d(\.\d+)?|90(\.0+)?),[-]?(180(\.0+)?|((1[0-7]\d)|([1-9]?\d))(\.\d+)?)$"""
}
