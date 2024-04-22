package com.misterjvm.weather.domain.errors

abstract class ApplicationException(message: String) extends RuntimeException(message)

// When client provides bad input for coordinates
final case class MalformedInputException(message: String) extends ApplicationException(message)

// When the upstream service returns an exception (i.e. coordinates are out of bounds of coverage)
final case class UpstreamServiceException(message: String) extends ApplicationException(message)
