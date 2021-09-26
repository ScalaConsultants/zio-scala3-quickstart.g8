package $package$

import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.console._
import zio.random._
import scala.util.Try
$if(add_metrics.truthy)$
import zio.zmx.prometheus.PrometheusClient
import zio.zmx._
import zio.zmx.diagnostics._
$endif$
import zio.config._
import zio.clock.Clock
import zio.logging._
import $package$.config.configuration.ServerConfig
import $package$.service._
import $package$.repo._
import $package$.api._
$if(add_metrics.truthy)$
import $package$.api.metricsdiagnostics._
$endif$
import $package$.config.configuration._

object Main extends zio.App:

  private val clockConsole = Clock.live ++ Console.live
  private val loggingEnv = clockConsole >>> Logging.console(
    logLevel = LogLevel.Info,
    format = LogFormat.ColoredLogFormat(),
  ) >>> Logging.withRootLoggerName("$name$")
  private val repoLayer = Random.live >>> ItemRepositoryLive.layer
  $if(add_websocket_endpoint.truthy)$
  private val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  $endif$
  private val businessLayer = repoLayer $if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> ItemServiceLive.layer
  $if(add_metrics.truthy)$
  private val diagnosticsConfigLayer = clockConsole ++ DiagnosticsServerConfig.layer
  private val zmxClient =
    diagnosticsConfigLayer >>> MetricsAndDiagnostics.zmxClientLayer
  private val diagnosticsLayer =
    diagnosticsConfigLayer >>> MetricsAndDiagnostics.diagnosticsLayer
  $endif$
  private val applicatonLayer =
    businessLayer ++ ServerChannelFactory.auto $if(add_metrics.truthy)$++ PrometheusClient.live ++ diagnosticsLayer ++ zmxClient $endif$

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)

    $if(add_metrics.truthy)$
    platform.withSupervisor(ZMXSupervisor)
    $endif$

    getConfig[ServerConfig]
      .flatMap(config =>
        setupServer(config.port)
          .make
          .use(_ => log.info(s"Server started on port \${config.port}") *> ZIO.never)
      )
      .provideLayer(
        applicatonLayer ++ ZEnv.live ++ ServerConfig.layer ++ EventLoopGroup.auto(
          nThreads
        ) ++ loggingEnv
      )
      .exitCode

  def setupServer(port: Int) =
    Server.port(port) ++
      Server.app(
        $if(add_metrics.truthy)$MetricsAndDiagnostics.exposeEndpoints +++$endif$ HttpRoutes.app $if(add_websocket_endpoint.truthy)$ +++ WebSocketRoute.socketImpl $endif$
      )