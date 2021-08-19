package $package$

import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.console._
import $package$.api._
import $package$.repo.itemrepository.ItemRepo
import zio.random.Random
import $package$.service.itemservice.BusinessLogic
import $package$.service.itemservice.BusinessLogic._
import $package$.domain.DomainError
import scala.util.Try

object Main extends zio.App:

  //TODO move to config with zio-config
  private val port = 8080
  private val repoLayer = (Random.live ++ Console.live) >>> ItemRepo.live
  private val businessLayer = repoLayer >>> BusinessLogic.live
  private val applicationLayer = businessLayer ++ ServerChannelFactory.auto

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    val nThreads: Int = args.headOption.flatMap(x => Try(x.toInt).toOption).getOrElse(0)

    server
      .make
      .use(_ => console.putStrLn(s"Server started on port \$port") *> ZIO.never)
      .provideCustomLayer(applicationLayer ++ EventLoopGroup.auto(nThreads))
      .exitCode

  val server: Server[BusinessLogic, Throwable] =
    Server.port(port) ++
      Server.app(HealthCheck.healthCheck +++ HttpRoutes.app)