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
import $package$.config.configuration.ServerConfig
import $package$.service._
import $package$.repo._
import $package$.api._
import $package$.api.metricsdiagnostics._

object Main extends zio.App:

  private val repoLayer = (Random.live ++ Console.live) >>> ItemRepositoryLive.layer
  $if(add_websocket_endpoint.truthy)$
  private val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  $endif$
  val businessLayer = repoLayer $if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> BusinessLogicServiceLive.layer
  private val diagnosticsLayer = (Clock.live ++ Console.live ++ ServerConfig.layer) >>> MetricsAndDiagnostics.layer
  private val applicatonLayer = businessLayer ++ ServerChannelFactory.auto ++ PrometheusClient.live ++ diagnosticsLayer

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)

    platform.withSupervisor(ZMXSupervisor)

    getConfig[ServerConfig]
      .flatMap(config =>
          setupServer(config.port)
            .make
            .use(_ => console.putStrLn(s"Server started on port \${config.port}") *> ZIO.never)
      )
      .provideLayer(applicatonLayer ++ ZEnv.live ++ ServerConfig.layer ++ EventLoopGroup.auto(nThreads))
      .exitCode

  def setupServer(port: Int) =
    Server.port(port) ++
      Server.app(MetricsAndDiagnostics.exposeEndpoints +++ HttpRoutes.app $if(add_websocket_endpoint.truthy)$ +++ WebSocketRoute.socketImpl $endif$)