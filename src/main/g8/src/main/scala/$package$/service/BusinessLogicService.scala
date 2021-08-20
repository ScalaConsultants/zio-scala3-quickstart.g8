package $package$.service

import zio._
import zio.stream._
import $package$.domain._

trait BusinessLogicService:
  def addItem(description: String): IO[DomainError, ItemId]

  def deleteItem(id: String): IO[DomainError, Unit]

  def deletedEvents(): Stream[Nothing, ItemId]

  def getAllItems(): IO[DomainError, List[Item]]

  def getItemById(id: String): IO[DomainError, Option[Item]]

  def getItemsByIds(ids: Set[String]): IO[DomainError, List[Item]]

  def updateItem(id: String, description: String): IO[DomainError, Unit]

object BusinessLogicService:
  def addItem(description: String): ZIO[Has[BusinessLogicService], DomainError, ItemId] =
    ZIO.serviceWith[BusinessLogicService](_.addItem(description))

  def deleteItem(id: String): ZIO[Has[BusinessLogicService], DomainError, Unit] =
    ZIO.serviceWith[BusinessLogicService](_.deleteItem(id))

  def deletedEvents(): ZStream[Has[BusinessLogicService], Nothing, ItemId] =
    ZStream.accessStream(_.get.deletedEvents())

  def getAllItems(): ZIO[Has[BusinessLogicService], DomainError, List[Item]] =
    ZIO.serviceWith[BusinessLogicService](_.getAllItems())

  def getItemById(id: String): ZIO[Has[BusinessLogicService], DomainError, Option[Item]] =
    ZIO.serviceWith[BusinessLogicService](_.getItemById(id))

  def getItemsByIds(ids: Set[String]): ZIO[Has[BusinessLogicService], DomainError, List[Item]] =
    ZIO.serviceWith[BusinessLogicService](_.getItemsByIds(ids))

  def updateItem(
      id: String,
      description: String,
    ): ZIO[Has[BusinessLogicService], DomainError, Unit] =
    ZIO.serviceWith[BusinessLogicService](_.updateItem(id, description))
