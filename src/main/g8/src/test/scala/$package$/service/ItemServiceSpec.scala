package $package$.service

import zio._
import zio.mock.Expectation._
import zio.stream._
import zio.test._
import zio.test.Assertion._
import $package$.domain._
import $package$.service._
import $package$.service.ItemService._
import $package$.repo._

object ItemServiceSpec extends ZIOSpecDefault:

  val exampleItem = Item(ItemId(123), "foo", BigDecimal(123))

  val getItemMock: ULayer[ItemRepository] = ItemRepoMock.GetById(
    equalTo(ItemId(123)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val getByNonExistingId: ULayer[ItemRepository] =
    ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val updateMock: ULayer[ItemRepository] =
    ItemRepoMock.Update(
      hasField("id", _._1, equalTo(exampleItem.id)),
      value(Some(())),
    ) ++ ItemRepoMock.Update(
      hasField("id", _._1, equalTo(ItemId(124))),
      value(None),
    )

  def spec = suite("item service test")(
    test("get item id accept long") {
      for {
        found   <- assertZIO(getItemById(ItemId(123)))(isSome(equalTo(exampleItem)))
        missing <- assertZIO(getItemById(ItemId(124)))(isNone)
      } yield found && missing
    }.provide(getItemMock, ItemServiceLive.layer),
    test("update item") {
      for {
        found   <- assertZIO(updateItem(ItemId(123), "foo", BigDecimal(123)))(
                     isSome(equalTo(Item(ItemId(123), "foo", BigDecimal(123))))
                   )
        missing <- assertZIO(updateItem(ItemId(124), "bar", BigDecimal(124)))(isNone)
      } yield found && missing
    }.provide(updateMock, ItemServiceLive.layer),
  )
