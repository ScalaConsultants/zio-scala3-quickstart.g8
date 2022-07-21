package $package$.api

import zhttp.http._
import zhttp.service._
import zio._
import zio.json._
import $package$.api.protocol._
import $package$.domain.{ DomainError, ItemId }
import $package$.service.ItemService
import $package$.service.ItemService._

import java.nio.charset.StandardCharsets

object HttpRoutes:

  val app: HttpApp[ItemService, Nothing] = Http.collectZIO {
    case Method.GET -> !! / "items" =>
      getAllItems().map(items =>
        Response.json(
          GetItems(items.map(item => GetItem(item.id.value, item.description))).toJson
        )
      )

    case Method.GET -> !! / "items" / id =>
      getItemById(ItemId(id.toLong))
        .map {
          case Some(item) => Response.json(GetItem(item.id.value, item.description).toJson)
          case None       => Response.status(Status.NotFound)
        }

    case Method.DELETE -> !! / "items" / id =>
      deleteItem(ItemId(id.toLong))
        .map(_ => Response.ok)

    case req @ Method.POST -> !! / "items" =>
      (for
        body <- entity[CreateItem](req)
          .absolve
          .tapError(_ => ZIO.logInfo(s"Unparseable body"))
        id <- addItem(body.description)
      yield GetItem(id.value, body.description)).either.map {
        case Right(created) =>
          Response(
            Status.Created,
            Headers(HeaderNames.contentType, HeaderValues.applicationJson),
            HttpData.fromString(created.toJson),
          )
        case Left(_) => Response.status(Status.BadRequest)
      }

    case req @ Method.PUT -> !! / "items" / id =>
      (for
        update <- entity[UpdateItem](req)
          .absolve
          .tapError(_ => ZIO.logInfo(s"Unparseable body "))
        _ <- updateItem(ItemId(id.toLong), update.description)
      yield ()).either.map {
        case Left(_)  => Response.status(Status.BadRequest)
        case Right(_) => Response.ok
      }
  }

  private def entity[T: JsonDecoder](req: Request): ZIO[Any, Throwable, Either[String, T]] =
    req.data.toByteBuf.map { byteBuf =>
      val bytes = Array[Byte]()
      byteBuf.readBytes(bytes)
      new String(bytes, StandardCharsets.UTF_8).fromJson[T]
    }
