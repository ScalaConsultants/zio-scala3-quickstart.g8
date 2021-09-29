package $package$.api.metricsdiagnostics

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

  val exposeEndpoints: HttpApp[Has[PrometheusClient] with Has[Console.Service] with Has[ZMXClient], Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "metrics" / "prometheus" =>
      PrometheusClient.snapshot.map(p => Response.text(p.value))

    case Method.GET -> Root / "diagnostics" / command =>
      for
        cmd  <- if (Set("dump", "test") contains command.trim) ZIO.succeed(command)
                  else ZIO.fail(new RuntimeException("Invalid command"))
                
        resp <- ZIO.service[ZMXClient].flatMap(config => config.sendCommand(Chunk(cmd)))
      yield Response.text(resp)

    case Method.GET -> Root / "health" =>
        ZIO.succeed(Response.status(Status.OK))
  }

  val diagnosticsLayer: ZLayer[Clock & Console & Has[DiagnosticsServerConfig], Exception, Has[Diagnostics]] = 
    ZLayer.service[DiagnosticsServerConfig].flatMap(config => Diagnostics.make(config.get.host, config.get.diagnosticsPort))
  
  val zmxClientLayer : ZLayer[Has[DiagnosticsServerConfig], Throwable, Has[ZMXClient]] =
    (for
      config <- ZIO.service[DiagnosticsServerConfig]
    yield (ZMXClient(ZMXConfig(config.host, config.diagnosticsPort, config.debug)))).toLayer