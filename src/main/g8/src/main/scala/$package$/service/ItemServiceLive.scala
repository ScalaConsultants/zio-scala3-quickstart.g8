package $package$.service

import zio._
import zio.stream._
import $package$.domain._
import $package$.repo._

final class ItemServiceLive(repo: ItemRepository) extends ItemService:
  def addItem(description: String): IO[DomainError, ItemId] =
    repo.add(description)

  def deleteItem(id: ItemId): IO[DomainError, Unit] =
    repo.delete(id)

  def getAllItems(): IO[DomainError, List[Item]] =
    repo.getAll()

  def getItemById(id: ItemId): IO[DomainError, Option[Item]] =
    repo.getById(id)

  def updateItem(id: ItemId, description: String): IO[DomainError, Unit] =
    for
      foundOption <- getItemById(id)
      _           <- ZIO
                       .fromOption(foundOption)
                       .mapError(_ => BusinessError(s"Item with ID \${id.value} not found"))
                       .flatMap(_ => repo.update(Item(id, description)))
    yield ()

object ItemServiceLive:
  val layer: URLayer[ItemRepository, ItemService] =
    ZLayer(ZIO.service[ItemRepository].map(ItemServiceLive(_)))
