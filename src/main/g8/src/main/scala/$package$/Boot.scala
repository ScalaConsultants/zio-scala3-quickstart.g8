package $package$

import $package$.api._
import $package$.api.healthcheck._
import $package$.config.Configuration._
import $package$.infrastructure._
import io.getquill.jdbczio.Quill
import io.getquill.Literal
import zio._
import zio.config._
import zio.http.Server
import zio.logging.backend.SLF4J

object Boot extends ZIOAppDefault:

  override val bootstrap: ULayer[Unit] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val dataSourceLayer = Quill.DataSource.fromPrefix("db")

  private val postgresLayer = Quill.Postgres.fromNamingStrategy(Literal)

  private val repoLayer = ItemRepositoryLive.layer

  private val healthCheckServiceLayer = HealthCheckServiceLive.layer

  private val serverLayer =
    ZLayer
      .service[ServerConfig]
      .flatMap { cfg =>
        Server.defaultWithPort(cfg.get.port)
      }
      .orDie

  val routes = HttpRoutes.app ++ HealthCheckRoutes.app

  private val program = Server.serve(routes)

  override val run =
    program.provide(
      healthCheckServiceLayer,
      serverLayer,
      ServerConfig.layer,
      repoLayer,
      postgresLayer,
      dataSourceLayer,
    )
