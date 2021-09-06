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
