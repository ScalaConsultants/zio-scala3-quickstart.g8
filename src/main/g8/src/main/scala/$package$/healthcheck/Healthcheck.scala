package $package$.healthcheck

import zio._
import zhttp.http._
import zio.zmx.metrics._

object Healthcheck:

  val expose: HttpApp[Any, Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "health" =>
        ZIO.succeed(Response.status(Status.OK)) @@  MetricAspect.count("healthcheck_counter")
  }