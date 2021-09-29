package $package$.service

import zio._
import zio.test._
import zio.test.Assertion._
import zio.test.mock.Expectation._
import $package$.domain._
import $package$.service._
import $package$.service.ItemService._
import $package$.repo._

object ItemServiceSpec extends DefaultRunnableSpec:

  $if(add_websocket_endpoint.truthy)$
  private val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  $endif$

  val exampleItem = Item(ItemId(123), "foo")

  val getItemMock: ULayer[Has[ItemRepository]] = ItemRepoMock.GetById(
    equalTo(ItemId(123)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val getByNonExistingId: ULayer[Has[ItemRepository]] =
    ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val updateSuccesfullMock: ULayer[Has[ItemRepository]] = ItemRepoMock.GetById(
    equalTo(ItemId(123)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.Update(equalTo((ItemId(123), exampleItem.copy(description = "bar"))))

  def spec = suite("item service test")(
    testM("get item id accept long") {
      for
        found <- assertM(getItemById("123"))(isSome(equalTo(exampleItem)))
        mising <- assertM(getItemById("124"))(isNone)
        unparseable <- assertM(getItemById("abc").run)(
          fails(equalTo(DomainError.BusinessError("Id abc is in incorrect form.")))
        )
      yield found && mising && unparseable
    }.provideCustomLayer(getItemMock $if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> ItemServiceLive.layer),
    suite("update item")(
      testM("non existing item") {
        assertM(updateItem("124", "bar").run)(
          fails(equalTo(DomainError.BusinessError("Item with ID 124 not found")))
        )
      }.provideCustomLayer(getByNonExistingId $if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> ItemServiceLive.layer),
      testM("update succesfull") {
        assertM(updateItem("123", "bar"))(isUnit)
      }.provideCustomLayer(updateSuccesfullMock $if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> ItemServiceLive.layer),
    ),
  )

  def testLayer: ULayer[Has[ItemService]] = 
    (for {
      ref <- Ref.make(Map.empty[String, String])
    } yield (new ItemService {

      def addItem(description: String): IO[DomainError, ItemId] =
        for {
          id <- ZIO.succeed(description.hashCode.abs.toLong)
          _ <- ref.update(m => m + (id.toString -> description))
        } yield (ItemId(id))

      def deleteItem(id: String): IO[DomainError, Unit] =
          ref.update(map => map.removed(id))

      def deletedEvents(): zio.stream.Stream[Nothing, ItemId] = zio.stream.ZStream.fromEffect(ZIO.succeed(ItemId(1L)))

      def getAllItems(): IO[DomainError, List[Item]] =
         ref.get.map(m => m.view.map{
           case (key, value) => Item(ItemId(key.toLong), value)
         }.toList)

      def getItemById(id: String): IO[DomainError, Option[Item]] =
        for
          map <- ref.get
          item  <- ZIO.fromOption(map.get(id)).mapError(_ => DomainError.BusinessError(message = "not found"))
                      .map(des => Item(ItemId(id.toLong), des))
        yield (Some(item))

      def updateItem(id: String, description: String): IO[DomainError, Unit] =
        ref.update(map => map.updated(id, description))
    })).toLayer
