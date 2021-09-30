package $package$.service

import zio._
import zio.stream._
import $package$.domain._

trait ItemService:
  def addItem(description: String): IO[DomainError, ItemId]

  def deleteItem(id: String): IO[DomainError, Unit]

  $if(add_websocket_endpoint.truthy)$
  def deletedEvents(): Stream[Nothing, ItemId]
  $endif$

  def getAllItems(): IO[DomainError, List[Item]]

  def getItemById(id: String): IO[DomainError, Option[Item]]

  def updateItem(id: String, description: String): IO[DomainError, Unit]

object ItemService:
  def addItem(description: String): ZIO[Has[ItemService], DomainError, ItemId] =
    ZIO.serviceWith[ItemService](_.addItem(description))

  def deleteItem(id: String): ZIO[Has[ItemService], DomainError, Unit] =
    ZIO.serviceWith[ItemService](_.deleteItem(id))

  $if(add_websocket_endpoint.truthy)$
  def deletedEvents(): ZStream[Has[ItemService], Nothing, ItemId] =
  ZStream.accessStream(_.get.deletedEvents())
  $endif$

  def getAllItems(): ZIO[Has[ItemService], DomainError, List[Item]] =
    ZIO.serviceWith[ItemService](_.getAllItems())

  def getItemById(id: String): ZIO[Has[ItemService], DomainError, Option[Item]] =
    ZIO.serviceWith[ItemService](_.getItemById(id))

  def updateItem(
      id: String,
      description: String,
    ): ZIO[Has[ItemService], DomainError, Unit] =
    ZIO.serviceWith[ItemService](_.updateItem(id, description))
