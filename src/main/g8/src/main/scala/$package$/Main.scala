package $package$

$if(add_graphql.truthy)$
import caliban._
import caliban.CalibanError.ValidationError
import caliban.{ CalibanError, GraphQLInterpreter }
$endif$
$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
import io.getquill.context.ZioJdbc.QDataSource
import io.getquill.context.ZioJdbc.QConnection
$endif$
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.blocking._
import zio.clock._
import zio.config._
import zio.console._
import zio.logging._
import zio.random._
import zio.stream._
$if(add_metrics.truthy)$
import zio.zmx.prometheus.PrometheusClient
import zio.zmx._
import zio.zmx.diagnostics._
$endif$
$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
import $package$.api._
$endif$
$if(add_metrics.truthy)$
import $package$.api.diagnostics._
$endif$
$if(add_graphql.truthy)$
import $package$.api.graphql._
$endif$
import $package$.config.configuration._
import $package$.healthcheck._
$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
import $package$.repo._
import $package$.service._
$endif$

import scala.util.Try

object Main extends zio.App:

  private val clockConsole = Clock.live ++ Console.live

  private val loggingEnv = 
    clockConsole >>> 
    Logging.console(LogLevel.Info, LogFormat.ColoredLogFormat()) >>> 
    Logging.withRootLoggerName("$name$")
  $if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
  private val connection =
    Blocking.live >>> (QDataSource.fromPrefix("postgres-db") >>> QDataSource.toConnection)
  private val repoLayer = (loggingEnv ++ connection) >>> ItemRepositoryLive.layer
  $if(add_websocket_endpoint.truthy)$
  private val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  $endif$
  private val businessLayer = repoLayer$if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> ItemServiceLive.layer
  $endif$
  
  $if(add_metrics.truthy)$
  private val diagnosticsConfigLayer = clockConsole ++ DiagnosticsServerConfig.layer
  private val zmxClient =
    diagnosticsConfigLayer >>> MetricsAndDiagnostics.zmxClientLayer
  private val diagnosticsLayer =
    diagnosticsConfigLayer >>> MetricsAndDiagnostics.diagnosticsLayer
  $endif$

  private val applicationLayer = loggingEnv$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$ ++ businessLayer$endif$
   $if(add_metrics.truthy)$++ PrometheusClient.live ++ diagnosticsLayer ++ zmxClient$endif$

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)

    $if(add_metrics.truthy)$
    platform.withSupervisor(ZMXSupervisor)
    $endif$

    val program = 
      $if(add_graphql.truthy)$
      for {
        config      <- getConfig[ServerConfig]
        interpreter <- GraphqlApi.api.interpreter
        _           <- setupServer(config.port, interpreter)
                         .make
                         .use(_ => log.info(s"Server started on port \${config.port}") *> ZIO.never)
      } yield ()
      $else$
      for {
        config <- getConfig[ServerConfig]
        _      <- setupServer(config.port)
                    .make
                    .use(_ => log.info(s"Server started on port \${config.port}") *> ZIO.never)
      } yield ()
      $endif$

    program
      .provideLayer(
        ZEnv.live ++ 
        ServerConfig.layer ++ 
        ServerChannelFactory.auto ++ 
        EventLoopGroup.auto(nThreads) ++
        applicationLayer
      )
      .exitCode

  $if(add_graphql.truthy)$
  def setupServer(
      port: Int, 
      interpreter: GraphQLInterpreter[Console with Clock with Has[ItemService], CalibanError]
    ) =
  $else$
  def setupServer(port: Int) =
  $endif$
    Server.port(port) ++
      Server.app(
        $if(add_metrics.truthy)$MetricsAndDiagnostics.exposeEndpoints +++$endif$
        $if(add_http_endpoint.truthy)$HttpRoutes.app +++$endif$
        $if(add_websocket_endpoint.truthy)$ WebSocketRoute.socketImpl +++$endif$
        $if(add_graphql.truthy)$ GraphqlRoute.route(interpreter) +++$endif$
        Healthcheck.expose
      )