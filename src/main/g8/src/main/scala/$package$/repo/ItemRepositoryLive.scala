package $package$.repo

import zio.random._
import zio.console._
import zio._
import $package$.domain._
import $package$.domain.DomainError.RepositoryError

// TODO switch Random and Ref[Map[ItemId, Item]] with some DBClient and store to DB not to in memory Map
// TODO switch console with zio-logging
final case class ItemRepositoryLive(
    random: Random.Service,
    console: Console.Service,
    dataRef: Ref[Map[ItemId, Item]],
  ) extends ItemRepository:

  def add(description: String): IO[RepositoryError, ItemId] =
    for {
      itemId <- random.nextLong.map(_.abs)
      id = ItemId(itemId)
      _ <- dataRef.update(map => map + (id -> Item(id, description)))
    } yield id

  def delete(id: ItemId): IO[RepositoryError, Unit] =
    dataRef.update(map => map - id)

  def getAll(): IO[RepositoryError, List[Item]] =
    for {
      itemsMap <- dataRef.get
    } yield (itemsMap.view.values.toList)

  def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    for {
      values <- dataRef.get
    } yield (values.get(id))

  def getByIds(ids: Set[ItemId]): IO[RepositoryError, List[Item]] =
    for {
      values <- dataRef.get
    } yield (values.filter(id => ids.contains(id._1)).view.values.toList)

  def update(id: ItemId, item: Item): IO[RepositoryError, Unit] =
    dataRef.update(map => map + (id -> item.copy(id = id)))

object ItemRepositoryLive:
  val layer: ZLayer[Has[Random.Service] with Has[Console.Service], Nothing, Has[ItemRepository]] =
    ZLayer.fromServicesM[Random.Service, Console.Service, Any, Nothing, ItemRepository](
      (random, console) =>
        Ref.make(Map.empty[ItemId, Item]).map(data => ItemRepositoryLive(random, console, data))
    )
