package $package$.service

import $package$.service.itemservice.BusinessLogic
import zio.test._
import zio.test.Assertion._
import $package$.domain._
import $package$.repo.ItemRepoMock
import $package$.repo.itemrepository._
import zio.test.mock.Expectation._
import zio._

object BusinessLogicSpec extends DefaultRunnableSpec:

  val exampleItem = Item(ItemId(123), "foo")

  val getItemMock: ULayer[ItemRepo] = ItemRepoMock.GetById(
    equalTo(ItemId(123)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val getByNonExistingId: ULayer[ItemRepo] = ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val updateSuccesfullMock: ULayer[ItemRepo] = ItemRepoMock.GetById(
    equalTo(ItemId(123)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.Update(equalTo((ItemId(123), exampleItem.copy(description = "bar"))))

  def spec = suite("business logic test")(
    testM("get item id accept long") {
      for
        found <- assertM(BusinessLogic.getItemById("123"))(isSome(equalTo(exampleItem)))
        mising <- assertM(BusinessLogic.getItemById("124"))(isNone)
        unparseable <- assertM(BusinessLogic.getItemById("abc").run)(
          fails(equalTo(DomainError.BusinessError("Id abc is in incorrect form.")))
        )
      yield found && mising && unparseable
    }.provideCustomLayer(getItemMock >>> BusinessLogic.live),
    suite("update item")(
      testM("non existing item") {
        assertM(BusinessLogic.updateItem("124", "bar").run)(
          fails(equalTo(DomainError.BusinessError("Item with ID 124 not found")))
        )
      }.provideCustomLayer(getByNonExistingId >>> BusinessLogic.live),
      testM("update succesfull") {
        assertM(BusinessLogic.updateItem("123", "bar"))(isUnit)
      }.provideCustomLayer(updateSuccesfullMock >>> BusinessLogic.live),
    ),
  )
