package $package$.repo

import zio._
import $package$.domain._

trait ItemRepository:
  def add(data: ItemData): IO[RepositoryError, ItemId]

  def delete(id: ItemId): IO[RepositoryError, Long]

  def getAll(): IO[RepositoryError, List[Item]]

  def getById(id: ItemId): IO[RepositoryError, Option[Item]]

  def update(itemId: ItemId, data: ItemData): IO[RepositoryError, Option[Unit]]

object ItemRepository:
  def add(data: ItemData): ZIO[ItemRepository, RepositoryError, ItemId] =
    ZIO.serviceWithZIO[ItemRepository](_.add(data))

  def delete(id: ItemId): ZIO[ItemRepository, RepositoryError, Long] =
    ZIO.serviceWithZIO[ItemRepository](_.delete(id))

  def getAll(): ZIO[ItemRepository, RepositoryError, List[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getAll())

  def getById(id: ItemId): ZIO[ItemRepository, RepositoryError, Option[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getById(id))

  def update(itemId: ItemId, data: ItemData): ZIO[ItemRepository, RepositoryError, Option[Unit]] =
    ZIO.serviceWithZIO[ItemRepository](_.update(itemId, data))
