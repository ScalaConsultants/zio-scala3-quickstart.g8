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
      def live(random: Random.Service, console: Console.Service): Service = new Service {

        private lazy val data = Ref.make[Map[Long, Item]](Map.empty)

        def add(description: String): IO[RepositoryError, ItemId] =
          for {
            itemId <- random.nextLong
            ref    <- data
            id = ItemId(itemId)
            _ <- ref.update(map => map + (itemId -> Item(id, description)))
          } yield id

        def delete(id: ItemId): IO[RepositoryError, Unit] =
          for {
            ref <- data
            _   <- ref.update(map => map - id.value)
          } yield ()

        def getAll(): IO[RepositoryError, List[Item]] =
          for {
            ref      <- data
            itemsMap <- ref.get
          } yield (itemsMap.view.values.toList)

        def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
          for {
            ref    <- data
            values <- ref.get
          } yield (values.get(id.value))

        def getByIds(ids: Set[ItemId]): IO[RepositoryError, List[Item]] =
          for {
            ref    <- data
            values <- ref.get
          } yield (values.filter(id => ids.map(_.value).contains(id._1)).view.values.toList)

        def update(id: ItemId, item: Item): IO[RepositoryError, Unit] =
          for {
            ref <- data
            _   <- ref.update(map => map + (id.value -> item.copy(id = id)))
          } yield ()
      }

    val live: ZLayer[Random with Console, Nothing, ItemRepo] =
      ZLayer.fromServices((random, console) => Service.live(random, console))

    def add(description: String): ZIO[ItemRepo, RepositoryError, ItemId] = ZIO.accessM(_.get.add(description))

    def delete(id: ItemId): ZIO[ItemRepo, RepositoryError, Unit] = ZIO.accessM(_.get.delete(id))

    def getAll(): ZIO[ItemRepo, RepositoryError, List[Item]] = ZIO.accessM(_.get.getAll())

    def getById(id: ItemId): ZIO[ItemRepo, RepositoryError, Option[Item]] = ZIO.accessM(_.get.getById(id))

    def getByIds(ids: Set[ItemId]): ZIO[ItemRepo, RepositoryError, List[Item]] = ZIO.accessM(_.get.getByIds(ids))

    def update(id: ItemId, item: Item): ZIO[ItemRepo, RepositoryError, Unit] = ZIO.accessM(_.get.update(id, item))