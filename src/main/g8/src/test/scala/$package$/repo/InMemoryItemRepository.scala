package $package$.repo

import zio._
import $package$.domain._

final class InMemoryItemRepository(
    random: Random,
    dataRef: Ref[Map[ItemId, Item]],
  ) extends ItemRepository:

  def add(description: String): IO[RepositoryError, ItemId] =
    for {
      itemId <- random.nextLong.map(_.abs)
      id      = ItemId(itemId)
      _      <- dataRef.update(map => map + (id -> Item(id, description)))
    } yield id

  def delete(id: ItemId): IO[RepositoryError, Long] =
    dataRef.modify { map =>
      if (!map.contains(id)) (0L, map)
      else (1L, map.removed(id))
    }

  def getAll(): IO[RepositoryError, List[Item]] =
    for {
      itemsMap <- dataRef.get
    } yield itemsMap.view.values.toList

  def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    for {
      values <- dataRef.get
    } yield values.get(id)

  def update(item: Item): IO[RepositoryError, Option[Unit]] =
    dataRef.modify { map =>
      if (!map.contains(item.id)) (None, map)
      else (Some(()), map.updated(item.id, item))
    }

object InMemoryItemRepository:
  val layer: ZLayer[Random, Nothing, ItemRepository] =
    ZLayer(for {
      random  <- ZIO.service[Random]
      dataRef <- Ref.make(Map.empty[ItemId, Item])
    } yield InMemoryItemRepository(random, dataRef))
