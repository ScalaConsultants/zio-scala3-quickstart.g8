package $package$

import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.console._
import zio.random._
import scala.util.Try
import zio.zmx.prometheus.PrometheusClient
import zio.zmx._
import zio.zmx.diagnostics._
import zio.config._
import zio.clock.Clock
import zio.logging._
import $package$.config.configuration.ServerConfig
import $package$.service._
import $package$.repo._
import $package$.api._
import $package$.api.metricsdiagnostics._
import $package$.config.configuration._

object Main extends zio.App:

  //TODO HttpRoutesSpec failing because of Logging mock
  //TODO make metrics optional

  private val clockConsole = Clock.live ++ Console.live
  private val loggingEnv = clockConsole >>> Logging.console(
    logLevel = LogLevel.Info,
    format = LogFormat.ColoredLogFormat(),
  ) >>> Logging.withRootLoggerName("zio-quickstart")
  private val repoLayer = Random.live >>> ItemRepositoryLive.layer
  private val subscriberLayer =
    ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  private val businessLayer = repoLayer ++ subscriberLayer >>> ItemServiceLive.layer
  private val zmxClient =
    (clockConsole ++ DiagnosticsServerConfig.layer) >>> MetricsAndDiagnostics.zmxClientLayer

  private val diagnosticsLayer =
    (clockConsole ++ DiagnosticsServerConfig.layer) >>> MetricsAndDiagnostics.diagnosticsLayer
  private val applicatonLayer =
    businessLayer ++ ServerChannelFactory.auto ++ PrometheusClient.live ++ diagnosticsLayer ++ zmxClient

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)

    platform.withSupervisor(ZMXSupervisor)

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
        MetricsAndDiagnostics.exposeEndpoints +++ HttpRoutes.app +++ WebSocketRoute.socketImpl
      )