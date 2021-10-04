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
$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
import zio.clock.Clock
import io.getquill.context.ZioJdbc.QDataSource
import zio.blocking.Blocking
import io.getquill.context.ZioJdbc.QConnection
$endif$
import zio.logging._
import $package$.config.configuration.ServerConfig
$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
import $package$.service._
import $package$.repo._
import $package$.api._
$endif$
$if(add_graphql.truthy)$
import $package$.api.graphql._
$endif$
$if(add_metrics.truthy)$
import $package$.api.diagnostics._
$endif$
import $package$.config.configuration._
import $package$.healthcheck._

object Main extends zio.App:

  private val clockConsole = Clock.live ++ Console.live
  private val loggingEnv = clockConsole >>> Logging.console(
    logLevel = LogLevel.Info,
    format = LogFormat.ColoredLogFormat(),
  ) >>> Logging.withRootLoggerName("$name$")
  $if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
  private val connection =
    Blocking.live >>> (QDataSource.fromPrefix("postgres-db") >>> QDataSource.toConnection)
  private val repoLayer = (loggingEnv ++ connection) >>> ItemRepositoryLive.layer
  $if(add_websocket_endpoint.truthy||add_graphql.truthy)$
  private val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  $endif$
  private val businessLayer = repoLayer $if(add_websocket_endpoint.truthy||add_graphql.truthy)$ ++ subscriberLayer $endif$ >>> ItemServiceLive.layer
  $endif$
  
  $if(add_metrics.truthy)$
  private val diagnosticsConfigLayer = clockConsole ++ DiagnosticsServerConfig.layer
  private val zmxClient =
    diagnosticsConfigLayer >>> MetricsAndDiagnostics.zmxClientLayer
  private val diagnosticsLayer =
    diagnosticsConfigLayer >>> MetricsAndDiagnostics.diagnosticsLayer
  $endif$

  private val applicatonLayer = loggingEnv $if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$++ businessLayer$endif$
   $if(add_metrics.truthy)$++ PrometheusClient.live ++ diagnosticsLayer ++ zmxClient$endif$

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
        $if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$applicatonLayer ++$endif$ ZEnv.live ++ ServerConfig.layer ++ ServerChannelFactory.auto ++ EventLoopGroup.auto(
          nThreads
        ) ++ applicatonLayer
      )
      .exitCode

  def setupServer(
      port: Int$if(add_graphql.truthy)$, 
      interpreter: GraphQLInterpreter[Console with Clock with Has[ItemService], CalibanError]$endif$
    ) =
    Server.port(port) ++
      Server.app(
        $if(add_metrics.truthy)$MetricsAndDiagnostics.exposeEndpoints +++$endif$
        $if(add_http_endpoint.truthy)$HttpRoutes.app +++$endif$
        $if(add_websocket_endpoint.truthy)$ WebSocketRoute.socketImpl +++$endif$  
        $if(add_graphql.truthy)$ GraphqlRoute.route(interpreter) +++$endif$ 
        Healthcheck.expose
      ) 