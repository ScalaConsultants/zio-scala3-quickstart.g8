package $package$

import io.getquill.jdbczio.Quill
import io.getquill.PluralizedTableNames
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.config._
import zio.stream._
import $package$.api._
import $package$.config.Configuration._
import $package$.repo._
import $package$.service._

object Main extends ZIOAppDefault:

  private val dataSourceLayer = Quill.DataSource.fromPrefix("postgres-db")

  private val postgresLayer = Quill.Postgres.fromNamingStrategy(PluralizedTableNames)

  private val repoLayer = ItemRepositoryLive.layer

  private val itemServiceLayer = ItemServiceLive.layer
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
      itemServiceLayer,
      repoLayer,
      postgresLayer,
      dataSourceLayer
    )
