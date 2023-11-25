package $package$.api

import $package$.api.healthcheck.HealthCheckService
import zio._
import zio.http._

object HealthCheckRoutes:

  val app: HttpApp[HealthCheckService] =
    Routes(
      Method.HEAD / "healthcheck" ->
        handler {
          ZIO.succeed {
            Response.status(Status.NoContent)
          }
        },

      Method.GET / "healthcheck" ->
        handler {
          HealthCheckService.check.map { dbStatus =>
            if (dbStatus.status) Response.ok
            else Response.status(Status.InternalServerError)
          }
        }
    ).toHttpApp
