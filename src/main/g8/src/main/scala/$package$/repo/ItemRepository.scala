package $package$.repo

import zio._
import $package$.domain._

trait ItemRepository:
  def add(description: String): IO[RepositoryError, ItemId]

  def delete(id: ItemId): IO[RepositoryError, Unit]

  def getAll(): IO[RepositoryError, List[Item]]

  def getById(id: ItemId): IO[RepositoryError, Option[Item]]

  def update(item: Item): IO[RepositoryError, Unit]

object ItemRepository:
  def add(description: String): ZIO[ItemRepository, RepositoryError, ItemId] =
    ZIO.serviceWithZIO[ItemRepository](_.add(description))

  def delete(id: ItemId): ZIO[ItemRepository, RepositoryError, Unit] =
    ZIO.serviceWithZIO[ItemRepository](_.delete(id))

  def getAll(): ZIO[ItemRepository, RepositoryError, List[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getAll())

  def getById(id: ItemId): ZIO[ItemRepository, RepositoryError, Option[Item]] =
    ZIO.serviceWithZIO[ItemRepository](_.getById(id))

  def update(item: Item): ZIO[ItemRepository, RepositoryError, Unit] =
    ZIO.serviceWithZIO[ItemRepository](_.update(item))
