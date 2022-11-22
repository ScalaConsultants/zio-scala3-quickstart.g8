package $package$.api

import zhttp.http._
import zhttp.service._
import zio._
import zio.json._
import $package$.api.protocol._
import $package$.api.Extensions._
import $package$.domain._
import $package$.service.ItemService
import $package$.service.ItemService._

import java.nio.charset.StandardCharsets

object HttpRoutes extends JsonSupport:

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

    case req @ Method.PUT -> !! / "items" / itemId =>
      val effect: ZIO[ItemService, DomainError, Item] =
        for {
          id                <- Utils.extractLong(itemId)
          updateItemRequest <- req.jsonBodyAs[UpdateItem]
          maybeItem         <- ItemService.updateItem(ItemId(id), updateItemRequest.description)
          item              <- maybeItem
                                 .map(ZIO.succeed(_))
                                 .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.either.map {
        case Left(_)     => Response.status(Status.BadRequest)
        case Right(item) =>
          Response
            .json(item.toJson)
            .setStatus(Status.Ok)
      }
  }

  private def entity[T: JsonDecoder](req: Request): ZIO[Any, Throwable, Either[String, T]] =
    req.bodyAsString.map(_.fromJson[T])
