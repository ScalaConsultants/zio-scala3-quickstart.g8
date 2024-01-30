package $package$.api

import java.nio.charset.StandardCharsets

import $package$.api.Extensions._
import $package$.application.ItemService
import $package$.domain._
import zio._
import zio.http._
import zio.json._

object HttpRoutes extends JsonSupport:

  val app: HttpApp[ItemRepository] = Routes(
    Method.GET / "items"                       -> handler { (_: Request) =>
      val effect: ZIO[ItemRepository, DomainError, List[Item]] =
        ItemService.getAllItems()

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    },
    Method.GET / "items" / string("itemId")    -> handler { (itemId: String, _: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          id        <- Utils.extractLong(itemId)
          maybeItem <- ItemService.getItemById(ItemId(id))
          item      <- maybeItem
                         .map(ZIO.succeed(_))
                         .getOrElse(ZIO.fail(NotFoundError))
        } yield item

      effect.foldZIO(Utils.handleError, _.toResponseZIO)

    },
    Method.DELETE / "items" / string("itemId") -> handler { (itemId: String, _: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Unit] =
        for {
          id     <- Utils.extractLong(itemId)
          amount <- ItemService.deleteItem(ItemId(id))
          _      <- if (amount == 0) ZIO.fail(NotFoundError)
                    else ZIO.unit
        } yield ()

      effect.foldZIO(Utils.handleError, _.toEmptyResponseZIO)

    },
    Method.POST / "items"                      -> handler { (req: Request) =>
      val effect: ZIO[ItemRepository, DomainError, Item] =
        for {
          createItem <- req.jsonBodyAs[CreateItemRequest]
          itemId     <- ItemService.addItem(createItem.name, createItem.price)
        } yield Item(itemId, createItem.name, createItem.price)

      effect.foldZIO(Utils.handleError, _.toResponseZIO(Status.Created))

    },
    Method.PUT / "items" / string("itemId")    -> handler { (itemId: String, req: Request) =>
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

    },
    Method.PATCH / "items" / string("itemId")  -> handler { (itemId: String, req: Request) =>
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
    },
  ).toHttpApp
