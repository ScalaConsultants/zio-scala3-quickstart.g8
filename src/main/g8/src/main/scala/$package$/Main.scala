package $package$

$if(add_graphql.truthy)$
import caliban._
import caliban.CalibanError.ValidationError
import caliban.{ CalibanError, GraphQLInterpreter }
$endif$
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.stream._
import zio.console._
import zio.random._
import zio.clock._
import zio.blocking._
import scala.util.Try
$if(add_metrics.truthy)$
import zio.zmx.prometheus.PrometheusClient
import zio.zmx._
import zio.zmx.diagnostics._
$endif$
import zio.config._
import zio.clock.Clock
import io.getquill.context.ZioJdbc.QDataSource
import zio.blocking.Blocking
import io.getquill.context.ZioJdbc.QConnection
import zio.logging._
import $package$.config.configuration.ServerConfig
import $package$.service._
import $package$.repo._
import $package$.api._
$if(add_graphql.truthy)$
import $package$.api.graphql._
$endif$
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
  private val connection =
    Blocking.live >>> (QDataSource.fromPrefix("testPostgresDB") >>> QDataSource.toConnection)
  private val repoLayer = (loggingEnv ++ connection) >>> ItemRepositoryLive.layer
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
        for {
          $if(add_graphql.truthy)$interpreter <- GraphqlApi.api.interpreter $endif$
          _ <- setupServer(config.port$if(add_graphql.truthy)$, interpreter$endif$)
            .make
            .use(_ => log.info(s"Server started on port \${config.port}") *> ZIO.never)
        } yield ()
      )
      .provideLayer(
        applicatonLayer ++ ZEnv.live ++ ServerConfig.layer ++ EventLoopGroup.auto(
          nThreads
        ) ++ loggingEnv
      )
      .exitCode

  def setupServer(
      port: Int$if(add_graphql.truthy)$, 
      interpreter: GraphQLInterpreter[Console with Clock with Has[ItemService], CalibanError]$endif$
    ) =
    Server.port(port) ++
      Server.app(
        $if(add_metrics.truthy)$MetricsAndDiagnostics.exposeEndpoints +++$endif$ HttpRoutes.app $if(add_websocket_endpoint.truthy)$ +++ WebSocketRoute.socketImpl $endif$  $if(add_graphql.truthy)$+++ GraphqlRoute
          .route(interpreter) $endif$
      )