package $package$.repo

import zio._
import $package$.domain._

final class InMemoryItemRepository(
    random: Random,
    storeRef: Ref[Map[ItemId, Item]],
  ) extends ItemRepository:

  def add(description: String): IO[RepositoryError, ItemId] =
    for {
      itemId <- random.nextLong.map(_.abs)
      id      = ItemId(itemId)
      _      <- storeRef.update(map => map + (id -> Item(id, description)))
    } yield id

  def delete(id: ItemId): IO[RepositoryError, Long] =
    storeRef.modify { store =>
      if (!store.contains(id)) (0L, store)
      else (1L, store.removed(id))
    }

  def getAll(): IO[RepositoryError, List[Item]] =
    for {
      store <- storeRef.get
    } yield store.view.values.toList

  def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    for {
      store <- storeRef.get
    } yield store.get(id)

  def update(item: Item): IO[RepositoryError, Option[Unit]] =
    storeRef.modify { store =>
      if (!store.contains(item.id)) (None, store)
      else (Some(()), store.updated(item.id, item))
    }

object InMemoryItemRepository:
  val layer: ZLayer[Random, Nothing, ItemRepository] =
    ZLayer(for {
      random   <- ZIO.service[Random]
      storeRef <- Ref.make(Map.empty[ItemId, Item])
    } yield InMemoryItemRepository(random, storeRef))
