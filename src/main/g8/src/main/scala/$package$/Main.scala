package $package$

import caliban._
import caliban.CalibanError.ValidationError
import caliban.{ CalibanError, GraphQLInterpreter }
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.console._
import zio.random._
import zio.clock._
import zio.blocking._
import scala.util.Try
import $package$.service._
import $package$.repo._
import $package$.api._

object Main extends zio.App:

  //TODO move to config with zio-config
  private val port = 8080
  private val repoLayer = (Random.live ++ Console.live) >>> ItemRepositoryLive.layer
  $if(add_websocket_endpoint.truthy)$
  private val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  $endif$
  val businessLayer = repoLayer $if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> ItemServiceLive.layer
  val applicationLayer = businessLayer ++ ServerChannelFactory.auto

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)
    (for {
      interpreter <- GraphqlApi.api.interpreter
      _ <- server(interpreter)
        .make
        .use(_ => putStrLn(s"Server started on port \$port") *> ZIO.never)
    } yield ()).provideCustomLayer(applicationLayer ++ EventLoopGroup.auto(nThreads)).exitCode

  def server( interpreter: GraphQLInterpreter[Console with Clock with Has[ItemService], CalibanError]
    ): Server[Console with Blocking with Clock with Has[ItemService], Throwable] =
    Server.port(port) ++
      Server.app(
        HealthCheck.healthCheck +++ HttpRoutes.app $if(add_websocket_endpoint.truthy)$ +++ WebSocketRoute.socketImpl $endif$ +++ GraphqlRoute
          .route(interpreter)
      )

  val server: Server[Console with Has[ItemService], Throwable] =
    Server.port(port) ++
      Server.app(HealthCheck.healthCheck +++ HttpRoutes.app $if(add_websocket_endpoint.truthy)$ +++ WebSocketRoute.socketImpl $endif$)