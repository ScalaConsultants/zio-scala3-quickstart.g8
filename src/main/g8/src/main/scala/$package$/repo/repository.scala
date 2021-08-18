package $package$.repo


import $package$.domain.DomainError.RepositoryError
import $package$.domain.Item
import $package$.domain.ItemId
import zio._
import zio.random._
import zio.console._

object itemrepository:

  type ItemRepo = Has[ItemRepo.Service]

  object ItemRepo:

    trait Service:
      def add(description: String): IO[RepositoryError, ItemId]

      def delete(id: ItemId): IO[RepositoryError, Unit]

      def getAll(): IO[RepositoryError, List[Item]]

      def getById(id: ItemId): IO[RepositoryError, Option[Item]]

      def getByIds(ids: Set[ItemId]): IO[RepositoryError, List[Item]]

      def update(id: ItemId, item: Item): IO[RepositoryError, Unit]

    object Service:
      // TODO switch Random with some DBClient and store to DB not to in memory Map
      // TODO switch console with zio-logging
      def live(random: Random.Service, console: Console.Service, dataRef: Ref[Map[ItemId, Item]]): Service = new Service {

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
          } yield (values.filter(id => ids.map(_.value).contains(id._1)).view.values.toList)

        def update(id: ItemId, item: Item): IO[RepositoryError, Unit] =
          for {
            _   <- dataRef.update(map => map + (id -> item.copy(id = id)))
          } yield ()
      }

    val live: ZLayer[Random with Console, Nothing, ItemRepo] =
      ZLayer.fromServicesM[Random.Service, Console.Service, Any, Nothing, ItemRepo.Service]((random, console) => Ref.make(Map.empty[ItemId, Item]).map(data => Service.live(random, console, data)))

    def add(description: String): ZIO[ItemRepo, RepositoryError, ItemId] = ZIO.accessM(_.get.add(description))

    def delete(id: ItemId): ZIO[ItemRepo, RepositoryError, Unit] = ZIO.accessM(_.get.delete(id))

    def getAll(): ZIO[ItemRepo, RepositoryError, List[Item]] = ZIO.accessM(_.get.getAll())

    def getById(id: ItemId): ZIO[ItemRepo, RepositoryError, Option[Item]] = ZIO.accessM(_.get.getById(id))

    def getByIds(ids: Set[ItemId]): ZIO[ItemRepo, RepositoryError, List[Item]] = ZIO.accessM(_.get.getByIds(ids))

    def update(id: ItemId, item: Item): ZIO[ItemRepo, RepositoryError, Unit] = ZIO.accessM(_.get.update(id, item))