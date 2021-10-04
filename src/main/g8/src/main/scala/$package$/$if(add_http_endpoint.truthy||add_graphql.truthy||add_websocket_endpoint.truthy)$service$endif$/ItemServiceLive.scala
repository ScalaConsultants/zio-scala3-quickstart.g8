package $package$.service

import zio._
import zio.stream._
import $package$.domain._
import $package$.domain.DomainError.BusinessError
import $package$.repo._

final class ItemServiceLive(repo: ItemRepository $if(add_websocket_endpoint.truthy||add_graphql.truthy)$, subscriber: SubscriberService $endif$) extends ItemService:
  def addItem(description: String): IO[DomainError, ItemId] =
    repo.add(description)

  def deleteItem(id: String): IO[DomainError, Unit] =
    for
      itemId <- formatId(id).map(ItemId(_))
      _ <- repo.delete(itemId)
      $if(add_websocket_endpoint.truthy||add_graphql.truthy)$ _ <- subscriber.publishDeleteEvents(itemId) $endif$
    yield ()

  $if(add_websocket_endpoint.truthy||add_graphql.truthy)$
  def deletedEvents(): Stream[Nothing, ItemId] =
    subscriber.showDeleteEvents
  $endif$

  def getAllItems(): IO[DomainError, List[Item]] =
    repo.getAll()

  def getItemById(id: String): IO[DomainError, Option[Item]] =
    for
      itemId <- formatId(id).map(ItemId(_))
      items <- repo.getById(itemId)
    yield items

  def updateItem(id: String, description: String): IO[DomainError, Unit] =
    for
      foundOption <- getItemById(id)
      _ <- ZIO
        .fromOption(foundOption)
        .mapError(_ => BusinessError(s"Item with ID \$id not found"))
        .flatMap(item => repo.update(item.id, Item(item.id, description)))
    yield ()

  private def formatId(id: String): IO[DomainError, Long] =
    ZIO.fromOption(id.toLongOption).mapError(_ => BusinessError(s"Id \$id is in incorrect form."))

object ItemServiceLive:
  val layer: URLayer[Has[ItemRepository] $if(add_websocket_endpoint.truthy||add_graphql.truthy)$ with Has[SubscriberService] $endif$, Has[ItemService]] =
    (ItemServiceLive(_$if(add_websocket_endpoint.truthy||add_graphql.truthy)$, _ $endif$)).toLayer
