package $package$.healthcheck

import zio._
import zhttp.http._
$if(add_metrics.truthy)$
import zio.zmx.metrics._
$endif$

object Healthcheck:

  val expose: HttpApp[Any, Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "health" =>
        ZIO.succeed(Response.status(Status.OK)) $if(add_metrics.truthy)$@@  MetricAspect.count("healthcheck_counter")$endif$
  }