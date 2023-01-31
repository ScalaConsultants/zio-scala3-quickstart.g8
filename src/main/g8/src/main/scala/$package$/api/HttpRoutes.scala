package $package$.api

import zhttp.http._
import zhttp.service._
import zio._
import zio.json._
import $package$.api.Extensions._
import $package$.domain._
import $package$.application.ItemService

import java.nio.charset.StandardCharsets

object HttpRoutes extends JsonSupport:

  val app: HttpApp[ItemRepository, Nothing] = Http.collectZIO {
    case Method.GET -> !! / "items" =>
      val effect: ZIO[ItemRepository, DomainError, List[Item]] =
        ItemService.getAllItems()

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    case Method.GET -> !! / "items" / itemId =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          id        <- Utils.extractLong(itemId)
          maybeItem <- ItemService.getItemById(ItemId(id))
          item      <- maybeItem
                         .map(ZIO.succeed(_))
                         .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    case Method.DELETE -> !! / "items" / itemId =>
      val effect: ZIO[ItemRepository, DomainError, Unit] =
        for {
          id     <- Utils.extractLong(itemId)
          amount <- ItemService.deleteItem(ItemId(id))
          _      <- if (amount == 0) ZIO.fail(NotFoundError)
                    else ZIO.unit
        } yield ()

      effect.foldZIO(Utils.handleError, _.toEmptyResponseZIO)

    case req @ Method.POST -> !! / "items" =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          createItem <- req.jsonBodyAs[CreateItemRequest]
          itemId     <- ItemService.addItem(createItem.name, createItem.price)
        } yield Item(itemId, createItem.name, createItem.price)

      effect.foldZIO(Utils.handleError, _.toResponseZIO(Status.Created))

    case req @ Method.PUT -> !! / "items" / itemId =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          id         <- Utils.extractLong(itemId)
          updateItem <- req.jsonBodyAs[UpdateItemRequest]
          maybeItem  <- ItemService.updateItem(ItemId(id), updateItem.name, updateItem.price)
          item       <- maybeItem
                          .map(ZIO.succeed(_))
                          .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    case req @ Method.PATCH -> !! / "items" / itemId =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          id                <- Utils.extractLong(itemId)
          partialUpdateItem <- req.jsonBodyAs[PartialUpdateItemRequest]
          maybeItem         <- ItemService.partialUpdateItem(
                                 id = ItemId(id),
                                 name = partialUpdateItem.name,
                                 price = partialUpdateItem.price,
                               )
          item              <- maybeItem
                                 .map(ZIO.succeed(_))
                                 .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

  }
