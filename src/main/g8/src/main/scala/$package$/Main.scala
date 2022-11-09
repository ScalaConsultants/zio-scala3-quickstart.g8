package $package$

$if(add_http_endpoint.truthy)$
import io.getquill.jdbczio.Quill
$endif$
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.config._
import zio.stream._
$if(add_http_endpoint.truthy)$
import $package$.api._
$endif$
import $package$.config.Configuration._
import $package$.healthcheck._
$if(add_http_endpoint.truthy)$
import $package$.repo._
import $package$.service._
$endif$

object Main extends ZIOAppDefault:

  $if(add_http_endpoint.truthy)$
  private val dataSourceLayer = Quill.DataSource.fromPrefix("postgres-db")

  private val repoLayer = ItemRepositoryLive.layer

  private val serviceLayer = ItemServiceLive.layer
  $endif$

  val routes =
    $if(add_http_endpoint.truthy)$HttpRoutes.app ++$endif$
      Healthcheck.routes

  val program =
    for
      config <- getConfig[ServerConfig]
      _      <- Server.start(config.port, routes)
    yield ()

  override val run =
    program.provide(
      ServerConfig.layer$if(add_http_endpoint.truthy) $,
      serviceLayer,
      repoLayer,
      dataSourceLayer$endif$,
    )
