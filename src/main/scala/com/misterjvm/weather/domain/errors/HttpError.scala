package com.misterjvm.weather.domain.errors

import sttp.model.StatusCode

final case class HttpError(
    statusCode: StatusCode,
    message: String,
    cause: Throwable
) extends RuntimeException(message, cause)

object HttpError {
  def decode(tuple: (StatusCode, String)): HttpError =
    HttpError(tuple._1, tuple._2, new RuntimeException(tuple._2))

  def encode(error: Throwable): (StatusCode, String) = error match {
    case MalformedInputException(message) => (StatusCode.BadRequest, message)
    // Assuming, the client did everything correct on our side, so still return a 200 ok
    case UpstreamServiceException(message) => (StatusCode.Ok, message)
    // TODO: surface out different statuses depending on the exception thrown
    case _ => (StatusCode.InternalServerError, error.getMessage)
  }
}
