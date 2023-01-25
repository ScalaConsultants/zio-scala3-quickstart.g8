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

  def delete(id: ItemId): IO[RepositoryError, Unit] =
    dataRef.update(map => map - id)

  def getAll(): IO[RepositoryError, List[Item]] =
    for {
      itemsMap <- dataRef.get
    } yield itemsMap.view.values.toList

  def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    for {
      values <- dataRef.get
    } yield values.get(id)

  def getByIds(ids: Set[ItemId]): IO[RepositoryError, List[Item]] =
    for {
      values <- dataRef.get
    } yield values.filter(id => ids.contains(id._1)).view.values.toList

  def update(item: Item): IO[RepositoryError, Unit] =
    dataRef.update(map => map + (item.id -> item))

object InMemoryItemRepository:
  val layer: ZLayer[Random, Nothing, ItemRepository] =
    ZLayer(for {
      random  <- ZIO.service[Random]
      dataRef <- Ref.make(Map.empty[ItemId, Item])
    } yield InMemoryItemRepository(random, dataRef))
