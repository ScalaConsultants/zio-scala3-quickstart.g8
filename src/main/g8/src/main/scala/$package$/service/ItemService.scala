package $package$.service

import zio._
import $package$.domain._

trait ItemService:
  def addItem(description: String): IO[DomainError, ItemId]

  def deleteItem(id: ItemId): IO[DomainError, Long]

  def getAllItems(): IO[DomainError, List[Item]]

  def getItemById(id: ItemId): IO[DomainError, Option[Item]]

  def updateItem(id: ItemId, description: String): IO[DomainError, Option[Item]]

  def partialUpdateItem(id: ItemId, description: Option[String]): IO[DomainError, Option[Item]]

object ItemService:
  def addItem(description: String): ZIO[ItemService, DomainError, ItemId] =
    ZIO.serviceWithZIO[ItemService](_.addItem(description))

  def deleteItem(id: ItemId): ZIO[ItemService, DomainError, Long] =
    ZIO.serviceWithZIO[ItemService](_.deleteItem(id))

  def getAllItems(): ZIO[ItemService, DomainError, List[Item]] =
    ZIO.serviceWithZIO[ItemService](_.getAllItems())

  def getItemById(id: ItemId): ZIO[ItemService, DomainError, Option[Item]] =
    ZIO.serviceWithZIO[ItemService](_.getItemById(id))

  def updateItem(
      id: ItemId,
      description: String,
    ): ZIO[ItemService, DomainError, Option[Item]] =
    ZIO.serviceWithZIO[ItemService](_.updateItem(id, description))

  def partialUpdateItem(id: ItemId, description: Option[String]): ZIO[ItemService, DomainError, Option[Item]] =
    ZIO.serviceWithZIO[ItemService](_.partialUpdateItem(id, description))
