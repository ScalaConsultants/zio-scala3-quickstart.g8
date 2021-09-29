package $package$.repo

import zio._
import zio.test.mock._
import $package$.domain.ItemId
import $package$.domain.Item
import $package$.domain.DomainError._
import $package$.repo.ItemRepository

object ItemRepoMock extends Mock[Has[ItemRepository]]:
  object Add extends Effect[String, Nothing, ItemId]
  object Delete extends Effect[ItemId, Nothing, Unit]
  object GetAll extends Effect[Unit, Nothing, List[Item]]
  object GetById extends Effect[ItemId, Nothing, Option[Item]]
  object Update extends Effect[(ItemId, Item), Nothing, Unit]

  val compose: URLayer[Has[Proxy], Has[ItemRepository]] =
    ZLayer.fromServiceM { proxy =>
      withRuntime.map { rts =>
        new ItemRepository {
          def add(description: String): IO[RepositoryError, ItemId] = proxy(Add, description)

          def delete(id: ItemId): IO[RepositoryError, Unit] = proxy(Delete, id)

          def getAll(): IO[RepositoryError, List[Item]] = proxy(GetAll)

          def getById(id: ItemId): IO[RepositoryError, Option[Item]] = proxy(GetById, id)

          def update(id: ItemId, item: Item): IO[RepositoryError, Unit] = proxy(Update, id, item)
        }
      }
    }
