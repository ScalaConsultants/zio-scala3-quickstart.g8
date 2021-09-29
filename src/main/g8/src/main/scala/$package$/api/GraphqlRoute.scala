package $package$.api

import caliban._
import caliban.CalibanError.ValidationError
import caliban.{ CalibanError, GraphQLInterpreter, ZHttpAdapter }
import $package$.service.ItemService
import zio._
import zio.stream._
import zhttp.http._
import zio.clock._
import zio.console._
import zio.blocking._

object GraphqlRoute {
  private val graphiql = Http.succeed(
    Response.http(content = HttpData.fromStream(ZStream.fromResource("graphql/graphiql.html")))
  )
  def route(
      interpreter: GraphQLInterpreter[Console with Clock with Has[
        ItemService
      ], CalibanError]
    ): RHttpApp[Console with Blocking with Clock with Has[ItemService]] =
    Http.route {
      case _ -> Root / "api" / "graphql" => ZHttpAdapter.makeHttpService(interpreter)
      case _ -> Root / "graphiql"        => graphiql
    }
}
