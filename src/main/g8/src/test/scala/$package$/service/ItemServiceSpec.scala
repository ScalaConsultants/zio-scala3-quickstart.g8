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

  val exampleItem = Item(ItemId(123), "foo")

  val getItemMock: ULayer[ItemRepository] = ItemRepoMock.GetById(
    equalTo(ItemId(123)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val getByNonExistingId: ULayer[ItemRepository] =
    ItemRepoMock.GetById(equalTo(ItemId(124)), value(None))

  val updateSuccesfullMock: ULayer[ItemRepository] = ItemRepoMock.GetById(
    equalTo(ItemId(123)),
    value(Some(exampleItem)),
  ) ++ ItemRepoMock.Update(equalTo(exampleItem.copy(description = "bar")))

  def spec = suite("item service test")(
    test("get item id accept long") {
      for
        found  <- assertZIO(getItemById(ItemId(123)))(isSome(equalTo(exampleItem)))
        mising <- assertZIO(getItemById(ItemId(124)))(isNone)
      yield found && mising
    }.provide(getItemMock, ItemServiceLive.layer),
    suite("update item")(
      test("non existing item") {
        assertZIO(updateItem(ItemId(124), "bar").exit)(
          fails(equalTo(BusinessError("Item with ID 124 not found")))
        )
      }.provide(getByNonExistingId, ItemServiceLive.layer),
      test("update succesfull") {
        assertZIO(updateItem(ItemId(123), "bar"))(isUnit)
      }.provide(updateSuccesfullMock, ItemServiceLive.layer),
    ),
  )

  def testLayer: ULayer[ItemService] =
    ZLayer(for {
      ref   <- Ref.make(Map.empty[String, String])
      queue <- Queue.bounded[ItemId](10)
    } yield new ItemService {

      def addItem(description: String): UIO[ItemId] =
        for {
          id <- ZIO.succeed(description.hashCode.abs.toLong)
          _  <- ref.update(m => m + (id.toString -> description))
        } yield ItemId(id)

      def deleteItem(id: ItemId): UIO[Unit] =
        ref.update(map => map.removed(id.value.toString())) <* queue.offer(id)

      def getAllItems(): UIO[List[Item]] =
        ref
          .get
          .map(m =>
            m.view
              .map {
                case (key, value) => Item(ItemId(key.toLong), value)
              }
              .toList
          )

      def getItemById(id: ItemId): UIO[Option[Item]] =
        for
          map  <- ref.get
          item <- ZIO
                    .succeed(map.get(id.value.toString()))
                    .map(op => op.map(des => Item(id, des)))
        yield item

      def updateItem(id: ItemId, description: String): IO[DomainError, Unit] =
        ref.update(map => map.updated(id.value.toString(), description))
    })
