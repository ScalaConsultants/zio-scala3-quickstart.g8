package $package$

import zhttp.http._
import zhttp.service.Server
import zio._

object HelloWorld extends zio.App:

  val healthCheck: HttpApp[Any, Nothing] = HttpApp.collect {
    case Method.GET -> Root / "health" => Response.status(Status.OK)
  }

  def run(args: List[String]) =
    Server.start(8080, healthCheck).exitCode