package $package$.healthcheck

import zio._
import zhttp.http._

object Healthcheck:

  //TODO return HeathCheckStatus
  val expose: HttpApp[Any, Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "health" =>
        ZIO.succeed(Response.status(Status.OK))
  }