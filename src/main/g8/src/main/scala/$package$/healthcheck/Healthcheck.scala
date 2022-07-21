package $package$.healthcheck

import zio._
import zhttp.http._

object Healthcheck:

  val routes: HttpApp[Any, Throwable] = Http.collect {
    case Method.GET -> !! / "health" =>
      Response.status(Status.NoContent)
  }
