package $package$.infrastructure

import $package$.domain._
import zio._

final class InMemoryItemRepository(
    random: Random,
    storeRef: Ref[Map[ItemId, ItemData]],
  ) extends ItemRepository:

  override def add(data: ItemData): IO[RepositoryError, ItemId] =
    for {
      itemId <- random.nextLong.map(_.abs)
      id      = ItemId(itemId)
      _      <- storeRef.update(store => store + (id -> data))
    } yield id

  override def delete(id: ItemId): IO[RepositoryError, Long] =
    storeRef.modify { store =>
      if (!store.contains(id)) (0L, store)
      else (1L, store.removed(id))
    }

  override def getAll(): IO[RepositoryError, List[Item]] =
    storeRef.get.map { store =>
      store.toList.map(kv => Item.withData(kv._1, kv._2))
    }

  override def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    for {
      store    <- storeRef.get
      maybeItem = store.get(id).map(data => Item.withData(id, data))
    } yield maybeItem

  override def update(id: ItemId, data: ItemData): IO[RepositoryError, Option[Unit]] =
    storeRef.modify { store =>
      if (!store.contains(id)) (None, store)
      else (Some(()), store.updated(id, data))
    }

object InMemoryItemRepository:
  val layer: ZLayer[Random, Nothing, ItemRepository] =
    ZLayer(for {
      random   <- ZIO.service[Random]
      storeRef <- Ref.make(Map.empty[ItemId, ItemData])
    } yield InMemoryItemRepository(random, storeRef))
