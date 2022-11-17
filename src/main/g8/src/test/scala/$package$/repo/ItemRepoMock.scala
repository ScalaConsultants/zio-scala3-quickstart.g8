package $package$.repo

import zio._
import zio.mock._
import $package$.domain._
import $package$.repo.ItemRepository

object ItemRepoMock extends Mock[ItemRepository]:
  object Add     extends Effect[String, Nothing, ItemId]
  object Delete  extends Effect[ItemId, Nothing, Unit]
  object GetAll  extends Effect[Unit, Nothing, List[Item]]
  object GetById extends Effect[ItemId, Nothing, Option[Item]]
  object Update  extends Effect[Item, Nothing, Unit]

  val compose: URLayer[Proxy, ItemRepository] =
    ZLayer.fromFunction { (proxy: Proxy) =>
      new ItemRepository {
        override def add(description: String) = proxy(Add, description)

        override def delete(id: ItemId) = proxy(Delete, id)

        override def getAll() = proxy(GetAll)

        override def getById(id: ItemId) = proxy(GetById, id)

        override def update(item: Item) = proxy(Update, item)
      }
    }
