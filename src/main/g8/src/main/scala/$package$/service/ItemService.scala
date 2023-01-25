package $package$.service

import zio._
import $package$.domain._

trait ItemService:
  def addItem(description: String): UIO[ItemId]

  def deleteItem(id: ItemId): UIO[Unit]

  def getAllItems(): UIO[List[Item]]

  def getItemById(id: ItemId): UIO[Option[Item]]

  def updateItem(id: ItemId, description: String): IO[DomainError, Unit]

object ItemService:
  def addItem(description: String): URIO[ItemService, ItemId] =
    ZIO.serviceWithZIO[ItemService](_.addItem(description))

  def deleteItem(id: ItemId): URIO[ItemService, Unit] =
    ZIO.serviceWithZIO[ItemService](_.deleteItem(id))

  def getAllItems(): URIO[ItemService, List[Item]] =
    ZIO.serviceWithZIO[ItemService](_.getAllItems())

  def getItemById(id: ItemId): URIO[ItemService, Option[Item]] =
    ZIO.serviceWithZIO[ItemService](_.getItemById(id))

  def updateItem(
      id: ItemId,
      description: String,
    ): ZIO[ItemService, DomainError, Unit] =
    ZIO.serviceWithZIO[ItemService](_.updateItem(id, description))
