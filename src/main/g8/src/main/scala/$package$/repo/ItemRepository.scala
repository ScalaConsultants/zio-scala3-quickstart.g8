package $package$.repo

import zio._
import $package$.domain._
import $package$.domain.DomainError.RepositoryError

trait ItemRepository:
  def add(description: String): IO[RepositoryError, ItemId]

  def delete(id: ItemId): IO[RepositoryError, Unit]

  def getAll(): IO[RepositoryError, List[Item]]

  def getById(id: ItemId): IO[RepositoryError, Option[Item]]

  def getByIds(ids: Set[ItemId]): IO[RepositoryError, List[Item]]

  def update(id: ItemId, item: Item): IO[RepositoryError, Unit]

object ItemRepository:
  def add(description: String): ZIO[Has[ItemRepository], RepositoryError, ItemId] = ZIO.serviceWith[ItemRepository](_.add(description))

  def delete(id: ItemId): ZIO[Has[ItemRepository], RepositoryError, Unit] = ZIO.serviceWith[ItemRepository](_.delete(id))

  def getAll(): ZIO[Has[ItemRepository], RepositoryError, List[Item]] = ZIO.serviceWith[ItemRepository](_.getAll())

  def getById(id: ItemId): ZIO[Has[ItemRepository], RepositoryError, Option[Item]] = ZIO.serviceWith[ItemRepository](_.getById(id))

  def getByIds(ids: Set[ItemId]): ZIO[Has[ItemRepository], RepositoryError, List[Item]] = ZIO.serviceWith[ItemRepository](_.getByIds(ids))

  def update(id: ItemId, item: Item): ZIO[Has[ItemRepository], RepositoryError, Unit] = ZIO.serviceWith[ItemRepository](_.update(id, item))