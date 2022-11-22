package $package$.api

import zio._
import zhttp.http._

import $package$.domain._
import $package$.api.Extensions._

private[api] object Utils:

  def extractLong(str: String): IO[ValidationError, Long] =
    ZIO
      .attempt(str.toLong)
      .refineToOrDie[NumberFormatException]
      .mapError(err => ValidationError(err.getMessage))

  def handleError(err: DomainError): UIO[Response] = err match {
    case NotFoundError          => ZIO.succeed(Response.status(Status.NotFound))
    case ValidationError(msg)   => msg.toResponseZIO(Status.BadRequest)
    case RepositoryError(cause) =>
      ZIO.logErrorCause(cause.getMessage, Cause.fail(cause)) *>
        "Internal server error, contact system administrator".toResponseZIO(Status.InternalServerError)
  }

end Utils
