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
      val effect: ZIO[ItemService, DomainError, List[Item]] =
        ItemService.getAllItems()

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    case Method.GET -> !! / "items" / itemId =>
      val effect: ZIO[ItemService, DomainError, Item] =
        for {
          id        <- Utils.extractLong(itemId)
          maybeItem <- ItemService.getItemById(ItemId(id))
          item      <- maybeItem
                         .map(ZIO.succeed(_))
                         .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    case Method.DELETE -> !! / "items" / itemId =>
      val effect: ZIO[ItemService, DomainError, Unit] =
        for {
          id     <- Utils.extractLong(itemId)
          amount <- ItemService.deleteItem(ItemId(id))
          _      <- if (amount == 0) ZIO.fail(NotFoundError)
                    else ZIO.unit
        } yield ()

      effect.foldZIO(Utils.handleError, _.toEmptyResponseZIO)

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

    case req @ Method.PATCH -> !! / "items" / itemId =>
      val effect: ZIO[ItemService, DomainError, Item] =
        for {
          id                <- Utils.extractLong(itemId)
          partialUpdateItem <- req.jsonBodyAs[PartialUpdateItem]
          maybeItem         <- ItemService.partialUpdateItem(ItemId(id), partialUpdateItem.description)
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
