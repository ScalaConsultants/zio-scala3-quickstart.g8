package $package$.api

import zhttp.http._

object HealthCheck:

    val healthCheck: HttpApp[Any, Nothing] = HttpApp.collect {
      case Method.GET -> Root / "health" =>
        Response.status(Status.OK)
    }