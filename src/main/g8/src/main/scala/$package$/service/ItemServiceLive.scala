package $package$.service

import zio._
import zio.stream._
import $package$.domain._
import $package$.repo._

final class ItemServiceLive(repo: ItemRepository) extends ItemService:
  def addItem(description: String): IO[DomainError, ItemId] =
    repo.add(description)

  def deleteItem(id: ItemId): IO[DomainError, Long] =
    repo.delete(id)

  def getAllItems(): IO[DomainError, List[Item]] =
    repo.getAll()

  def getItemById(id: ItemId): IO[DomainError, Option[Item]] =
    repo.getById(id)

  def updateItem(id: ItemId, description: String): IO[DomainError, Option[Item]] =
    for {
      item         <- ZIO.succeed(Item(id, description))
      maybeUpdated <- repo.update(item)
    } yield maybeUpdated.map(_ => item)

  def partialUpdateItem(id: ItemId, description: Option[String]): IO[DomainError, Option[Item]] =
    repo.getById(id).flatMap {
      case None              => ZIO.succeed(None)
      case Some(currentItem) =>
        val nextItem = Item(id, description.getOrElse(currentItem.description))
        repo
          .update(nextItem)
          .map(_ => Some(nextItem))
    }

object ItemServiceLive:
  val layer: URLayer[ItemRepository, ItemService] =
    ZLayer(ZIO.service[ItemRepository].map(ItemServiceLive(_)))
