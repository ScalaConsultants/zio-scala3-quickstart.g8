package $package$.api

import zio.*
import zio.json.*
import zhttp.http.*

import $package$.domain.ValidationError

private[api] object Extensions:

  implicit class RichRequest(val request: Request) extends AnyVal {
    def jsonBodyAs[T: JsonDecoder]: IO[ValidationError, T] =
      for {
        body: String <- request.bodyAsString.orDie
        t            <- ZIO.succeed(body.fromJson[T]).absolve.mapError(ValidationError.apply)
      } yield t
  }

end Extensions
