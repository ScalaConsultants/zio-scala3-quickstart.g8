package $package$.service

import zio._
import $package$.domain._
import $package$.domain.DomainError.BusinessError
import $package$.repo._

final case class BusinessLogicServiceLive(repo: ItemRepository) extends BusinessLogicService:
  def addItem(description: String): IO[DomainError, ItemId] =
    repo.add(description)

  def deleteItem(id: String): IO[DomainError, Unit] =
    for
      itemId <- formatId(id).map(ItemId(_))
      _ <- repo.delete(itemId)
    yield ()

  def getAllItems(): IO[DomainError, List[Item]] =
    repo.getAll()

  def getItemById(id: String): IO[DomainError, Option[Item]] =
    for
      itemId <- formatId(id).map(ItemId(_))
      items <- repo.getById(itemId)
    yield items

  def getItemsByIds(ids: Set[String]): IO[DomainError, List[Item]] =
    for
      itemIds <- ZIO.foreach(ids)(id => formatId(id))
      items <- repo.getByIds(itemIds.map(ItemId(_)))
    yield items

  def updateItem(id: String, description: String): IO[DomainError, Unit] =
    for
      foundOption <- getItemById(id)
      _ <- ZIO
        .fromOption(foundOption)
        .mapError(_ => BusinessError(s"Item with ID \$id not found"))
        .flatMap(item => repo.update(item.id, Item(item.id, description)))
    yield ()

  private def formatId(id: String): IO[DomainError, Long] =
    ZIO.fromOption(id.toLongOption).mapError(_ => BusinessError(s"Id \$id is in incorrect form."))

object BusinessLogicServiceLive:
  val layer: URLayer[Has[ItemRepository], Has[BusinessLogicService]] =
    (BusinessLogicServiceLive(_)).toLayer
