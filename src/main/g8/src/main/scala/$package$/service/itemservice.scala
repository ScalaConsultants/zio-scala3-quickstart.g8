package $package$.service

import zio._
import $package$.domain.DomainError.BusinessError
import $package$.domain.DomainError
import $package$.domain.ItemId
import $package$.domain.Item
import $package$.repo.itemrepository.ItemRepo

object itemservice:

  type BusinessLogic = Has[BusinessLogic.Service]

  object BusinessLogic:
    trait Service:
      def addItem(description: String): IO[DomainError, ItemId]

      def deleteItem(id: String): IO[DomainError, Unit]

      def getAllItems(): IO[DomainError, List[Item]]

      def getItemById(id: String): IO[DomainError, Option[Item]]

      def getItemsByIds(ids: Set[String]): IO[DomainError, List[Item]]

      def updateItem(id: String, description: String): IO[DomainError, Unit]

    object Service:

      def live(repo: ItemRepo.Service): Service = new Service {

        def addItem(description: String): IO[DomainError, ItemId] =
          repo.add(description)

        def deleteItem(id: String): IO[DomainError, Unit] =
          for {
            itemId <- formatId(id).map(ItemId(_))
            _      <- repo.delete(itemId)
          } yield ()

        def getAllItems(): IO[DomainError, List[Item]] =
          repo.getAll()

        def getItemById(id: String): IO[DomainError, Option[Item]] =
          for {
            itemId <- formatId(id).map(ItemId(_))
            items  <- repo.getById(itemId)
          } yield items

        def getItemsByIds(ids: Set[String]): IO[DomainError, List[Item]] =
          for {
            itemIds <- ZIO.foreach(ids)(id => formatId(id))
            items   <- repo.getByIds(itemIds.map(ItemId(_)))
          } yield items

        def updateItem(id: String, description: String): IO[DomainError, Unit] =
          for {
            foundOption <- getItemById(id)
            _           <- ZIO
              .fromOption(foundOption)
              .mapError(_ => BusinessError(s"Item with ID \${id} not found"))
              .flatMap(item => repo.update(item.id, Item(item.id, description)))
          } yield ()

        private def formatId(id: String): IO[DomainError, Long] =
          ZIO.fromOption(id.toLongOption).mapError(_ => BusinessError(s"Id \${id} is in incorrect form."))
      }
    

    val live: ZLayer[ItemRepo, Nothing, BusinessLogic] =
      ZLayer.fromService(repo => Service.live(repo))

    def addItem(description: String): ZIO[BusinessLogic, DomainError, ItemId] = ZIO.accessM(_.get.addItem(description))

    def deleteItem(id: String): ZIO[BusinessLogic, DomainError, Unit] = ZIO.accessM(_.get.deleteItem(id))

    def getAllItems(): ZIO[BusinessLogic, DomainError, List[Item]] = ZIO.accessM(_.get.getAllItems())

    def getItemById(id: String): ZIO[BusinessLogic, DomainError, Option[Item]] = ZIO.accessM(_.get.getItemById(id))

    def getItemsByIds(ids: Set[String]): ZIO[BusinessLogic, DomainError, List[Item]] =
      ZIO.accessM(_.get.getItemsByIds(ids))

    def updateItem(id: String, description: String): ZIO[BusinessLogic, DomainError, Unit] =
      ZIO.accessM(_.get.updateItem(id, description))
  

