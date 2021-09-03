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
import $package$.config.configuration.ServerConfig

object MetricsAndDiagnostics:

  //TODO create ZMXConfig out of config values
  private val zmxClient = new ZMXClient(ZMXConfig("localhost", 8081, false))

  //TODO make ZMX metrics & diagnostics optional
  val exposeEndpoints: HttpApp[Has[PrometheusClient] with Has[Console.Service], Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "metrics" / "prometheus" =>
      PrometheusClient.snapshot.map(p => Response.text(p.value))

    case Method.GET -> Root / "diagnostics" / command =>
      for
        cmd  <- if (Set("dump", "test") contains command.trim) ZIO.succeed(command)
                  else ZIO.fail(new RuntimeException("Invalid command"))
        resp <- zmxClient.sendCommand(Chunk(cmd))
      yield Response.text(resp)

    case Method.GET -> Root / "health" =>
        ZIO.succeed(Response.status(Status.OK))
  }

  val layer: ZLayer[Clock & Console & Has[ServerConfig], Exception, Has[Diagnostics]] =
    ZLayer.service[ServerConfig].flatMap(config => Diagnostics.make("localhost", config.get.diagnosticsPort))
  