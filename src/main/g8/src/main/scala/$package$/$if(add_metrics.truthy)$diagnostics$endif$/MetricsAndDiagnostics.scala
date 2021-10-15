package $package$.api.diagnostics

import zhttp.http._
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console._
import zio.duration.durationInt
import zio.zmx.metrics._
import zio.zmx._
import zio.zmx.prometheus.PrometheusClient
import java.io.IOException
import zio.zmx.diagnostics.ZMXClient
import zio.zmx.diagnostics.ZMXConfig
import zio.zmx.diagnostics._
import $package$.config.configuration.DiagnosticsServerConfig

object MetricsAndDiagnostics:

  val exposeMetrics: HttpApp[Has[PrometheusClient] with Has[Console.Service], Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "metrics" =>
      PrometheusClient.snapshot.map(p => Response.text(p.value))
  }

  val diagnosticsLayer: ZLayer[Clock & Console & Has[DiagnosticsServerConfig], Exception, Has[Diagnostics]] = 
    ZLayer.service[DiagnosticsServerConfig].flatMap(config => Diagnostics.make(config.get.host, config.get.diagnosticsPort))
  