package $package$.api

import zhttp.http.*
import zio.*

import $package$.api.healthcheck.HealthCheckService

object HealthCheckRoutes:

  val app: HttpApp[HealthCheckService, Nothing] = Http.collectZIO {

  case Method.HEAD -> !! / "healthcheck" =>
    ZIO.succeed {
      Response.status(Status.NoContent)
    }

  case Method.GET -> !! / "healthcheck" =>
    HealthCheckService.check.map { dbStatus =>
      if (dbStatus.status) Response.ok
      else Response.status(Status.InternalServerError)
    }
  }
