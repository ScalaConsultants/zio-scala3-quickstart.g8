package $package$.api

import zhttp.http._
import zhttp.service._
import zio._
import zio.json._
import $package$.api.protocol._
import $package$.api.Extensions._
import $package$.domain.{DomainError, ItemId, ValidationError}
import $package$.service.ItemService
import $package$.service.ItemService._

import java.nio.charset.StandardCharsets

object HttpRoutes:

  val app: HttpApp[ItemService, Nothing] = Http.collectZIO {
    case Method.GET -> !! / "items" =>
      getAllItems()
        .mapError(_.asThrowable)
        .orDie
        .map(items =>
          Response.json(
            GetItems(items.map(item => GetItem(item.id.value, item.description))).toJson
          )
        )

    case Method.GET -> !! / "items" / id =>
      getItemById(ItemId(id.toLong))
        .mapError(_.asThrowable)
        .orDie
        .map {
          case Some(item) => Response.json(GetItem(item.id.value, item.description).toJson)
          case None       => Response.status(Status.NotFound)
        }

    case Method.DELETE -> !! / "items" / id =>
      deleteItem(ItemId(id.toLong))
        .mapError(_.asThrowable)
        .orDie
        .map(_ => Response.ok)

    case req @ Method.POST -> !! / "items" =>
      val effect: ZIO[ItemService, DomainError, GetItem] =
        for {
          createItem <- req.jsonBodyAs[CreateItem]
          itemId     <- ItemService.addItem(createItem.description)
        } yield GetItem(itemId.value, createItem.description)

      effect.either.map {
        case Right(created) =>
          Response(
            Status.Created,
            Headers(HeaderNames.contentType, HeaderValues.applicationJson),
            HttpData.fromString(created.toJson),
          )
        case Left(_)        => Response.status(Status.BadRequest)
      }

    case req @ Method.PUT -> !! / "items" / id =>
      (for
        update <- entity[UpdateItem](req)
                    .absolve
                    .tapError(_ => ZIO.logInfo(s"Unparseable body "))
        _      <- updateItem(ItemId(id.toLong), update.description)
      yield ()).either.map {
        case Left(_)  => Response.status(Status.BadRequest)
        case Right(_) => Response.ok
      }
  }

  private def entity[T: JsonDecoder](req: Request): ZIO[Any, Throwable, Either[String, T]] =
    req.bodyAsString.map(_.fromJson[T])
