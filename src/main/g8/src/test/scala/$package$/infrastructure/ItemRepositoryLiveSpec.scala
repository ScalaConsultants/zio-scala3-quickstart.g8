package $package$.infrastructure

import io.getquill.Literal
import io.getquill.jdbczio.Quill
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio._
import $package$.domain._
import $package$.infrastructure.postgresql._

object ItemRepositoryLiveSpec extends ZIOSpecDefault:

  val containerLayer = ZLayer.scoped(PostgresContainer.make())

  val dataSourceLayer = ZLayer(ZIO.service[DataSourceBuilder].map(_.dataSource))

  val postgresLayer = Quill.Postgres.fromNamingStrategy(Literal)

  val repoLayer = ItemRepositoryLive.layer

  override def spec =
    suite("item repository test with postgres test container")(
      test("save items returns their ids") {
        for {
          id1 <- ItemRepository.add(ItemData("first item", BigDecimal(1)))
          id2 <- ItemRepository.add(ItemData("second item", BigDecimal(2)))
          id3 <- ItemRepository.add(ItemData("third item", BigDecimal(3)))

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
        } yield assert(item)(isSome) &&
        assert(item.get.name)(equalTo("second item")) &&
        assert(item.get.price)(equalTo(BigDecimal("2")))
      },
      test("update item 3") {
        for {
          _    <- ItemRepository.update(ItemId(3), ItemData("updated item", BigDecimal(3)))
          item <- ItemRepository.getById(ItemId(3))
        } yield assert(item)(isSome) &&
        assert(item.get.name)(equalTo("updated item")) &&
        assert(item.get.price)(equalTo(BigDecimal(3)))
      },
    ).provideShared(
      containerLayer,
      DataSourceBuilderLive.layer,
      dataSourceLayer,
      postgresLayer,
      repoLayer,
    ) @@ sequential
