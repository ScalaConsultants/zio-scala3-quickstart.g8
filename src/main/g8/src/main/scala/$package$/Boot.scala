package $package$

import io.getquill.jdbczio.Quill
import io.getquill.Literal
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.config._
import zio.stream._
import zio.logging.LogFormat
import zio.logging.backend.SLF4J
import $package$.api._
import $package$.api.healthcheck._
import $package$.config.Configuration._
import $package$.infrastructure._

object Boot extends ZIOAppDefault:

  override val bootstrap: ULayer[Unit] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val dataSourceLayer = Quill.DataSource.fromPrefix("db")

  private val postgresLayer = Quill.Postgres.fromNamingStrategy(Literal)

  private val repoLayer = ItemRepositoryLive.layer

  private val healthCheckServiceLayer = HealthCheckServiceLive.layer

  val routes = HttpRoutes.app ++ HealthCheckRoutes.app

  val program =
    for
      config <- getConfig[ServerConfig]
      _      <- Server.start(config.port, routes)
    yield ()

  override val run =
    program.provide(
      healthCheckServiceLayer,
      ServerConfig.layer,
      repoLayer,
      postgresLayer,
      dataSourceLayer
    )
