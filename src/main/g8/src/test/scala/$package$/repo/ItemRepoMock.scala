package $package$.repo

import zio.test.mock._
import $package$.domain.ItemId
import $package$.domain.Item
import $package$.domain.DomainError._
import $package$.repo.itemrepository.ItemRepo
import zio._

object ItemRepoMock extends Mock[ItemRepo]:
  object Add extends Effect[String, Nothing, ItemId]
  object Delete extends Effect[ItemId, Nothing, Unit]
  object GetAll extends Effect[Unit, Nothing, List[Item]]
  object GetById extends Effect[ItemId, Nothing, Option[Item]]
  object GetByIds extends Effect[Set[ItemId], Nothing, List[Item]]
  object Update extends Effect[(ItemId, Item), Nothing, Unit]

  val compose: URLayer[Has[Proxy], ItemRepo] =
    ZLayer.fromServiceM { proxy =>
      withRuntime.map { rts =>
        new ItemRepo.Service {
          def add(description: String): IO[RepositoryError, ItemId] = proxy(Add, description)

          def delete(id: ItemId): IO[RepositoryError, Unit] = proxy(Delete, id)

          def getAll(): IO[RepositoryError, List[Item]] = proxy(GetAll)

          def getById(id: ItemId): IO[RepositoryError, Option[Item]] = proxy(GetById, id)

          def getByIds(ids: Set[ItemId]): IO[RepositoryError, List[Item]] = proxy(GetByIds, ids)

          def update(id: ItemId, item: Item): IO[RepositoryError, Unit] = proxy(Update, id, item)
        }
      }
    }
