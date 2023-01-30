package $package$.service

import zio._
import zio.stream._
import $package$.domain._

final class ItemServiceLive(repo: ItemRepository) extends ItemService:
  override def addItem(name: String, price: BigDecimal): IO[DomainError, ItemId] =
    repo.add(ItemData(name, price))

  override def deleteItem(id: ItemId): IO[DomainError, Long] =
    repo.delete(id)

  override def getAllItems(): IO[DomainError, List[Item]] =
    repo.getAll()

  override def getItemById(id: ItemId): IO[DomainError, Option[Item]] =
    repo.getById(id)

  override def updateItem(
      id: ItemId,
      name: String,
      price: BigDecimal,
    ): IO[DomainError, Option[Item]] =
    for {
      data         <- ZIO.succeed(ItemData(name, price))
      maybeUpdated <- repo.update(id, data)
    } yield maybeUpdated.map(_ => Item.withData(id, data))

  override def partialUpdateItem(
      id: ItemId,
      name: Option[String],
      price: Option[BigDecimal],
    ): IO[DomainError, Option[Item]] =
    repo.getById(id).flatMap {
      case None              => ZIO.succeed(None)
      case Some(currentItem) =>
        val data = ItemData(name.getOrElse(currentItem.name), price.getOrElse(currentItem.price))
        repo
          .update(id, data)
          .map(_ => Some(Item.withData(id, data)))
    }

object ItemServiceLive:
  val layer: URLayer[ItemRepository, ItemService] =
    ZLayer(ZIO.service[ItemRepository].map(ItemServiceLive(_)))
