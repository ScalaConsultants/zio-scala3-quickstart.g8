package $package$.repo

import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio._
import $package$.repo.postgresql._
import $package$.domain._

object PostgresRunnableSpec extends ZIOSpecDefault:

  val containerLayer = ZLayer.scoped(PostgresContainer.make())

  val dataSourceLayer =
    DataSourceBuilderLive
      .layer
      .flatMap(builder => ZLayer.fromFunction(() => builder.get.dataSource))

  val repoLayer = ItemRepositoryLive.layer

  override def spec =
    suite("item repository test with postgres test container")(
      test("save items returns their ids") {
        for {
          id1 <- ItemRepository.add("first item")
          id2 <- ItemRepository.add("second item")
          id3 <- ItemRepository.add("third item")

        } yield assert(id1)(equalTo(ItemId(1))) && assert(id2)(equalTo(ItemId(2))) && assert(id3)(
          equalTo(ItemId(3))
        )
      },
      test("get all returns 3 items") {
        for {
          items <- ItemRepository.getAll()
        } yield assert(items)(hasSize(equalTo(3)))
      },
      test("delete first item") {
        for {
          _    <- ItemRepository.delete(ItemId(1))
          item <- ItemRepository.getById(ItemId(1))
        } yield assert(item)(isNone)
      },
      test("get item 2") {
        for {
          item <- ItemRepository.getById(ItemId(2))
        } yield assert(item)(isSome) && assert(item.get.description)(equalTo("second item"))
      },
      test("update item 3") {
        for {
          _    <- ItemRepository.update(Item(ItemId(3), "updated item"))
          item <- ItemRepository.getById(ItemId(3))
        } yield assert(item)(isSome) && assert(item.get.description)(equalTo("updated item"))
      },
    ).provideShared(containerLayer, dataSourceLayer, repoLayer) @@ sequential
