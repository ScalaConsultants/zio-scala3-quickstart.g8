package $package$.repo

import zio.test._
import zio.test.Assertion._
import zio.blocking._
import zio.clock._
import zio.test.TestAspect._
import zio._
import com.dimafeng.testcontainers.PostgreSQLContainer
import zio.logging.Logging
import zio.test.environment.TestEnvironment
import io.getquill.context.ZioJdbc.QConnection
import $package$.repo.postgresql._
import $package$.domain._

object PostgresRunnableSpec extends DefaultRunnableSpec:

  val containerLayer = PostgresContainer.make() ++ Blocking.live ++ Clock.live

  val connectionLayer =
    Blocking.live ++ (Blocking.live >>> containerLayer >>> ConnectionBuilderLive
      .layer
      .flatMap(builder => ZLayer.fromManaged(builder.get.connection)))

  val repoLayer = ItemRepositoryLive.layer

  val testLayer = (Logging.ignore ++ connectionLayer) >>> repoLayer

  override def spec =
    suite("item repository test with postgres test container")(
      testM("save items returns their ids") {
        for {
          id1 <- ItemRepository.add("first item")
          id2 <- ItemRepository.add("second item")
          id3 <- ItemRepository.add("third item")

        } yield (assert(id1)(equalTo(ItemId(1))) && assert(id2)(equalTo(ItemId(2))) && assert(id3)(
          equalTo(ItemId(3))
        ))
      },
      testM("get all returns 3 items") {
        for {
          items <- ItemRepository.getAll()
        } yield (assert(items)(hasSize(equalTo(3))))
      },
      testM("delete first item") {
        for {
          _ <- ItemRepository.delete(ItemId(1))
          item <- ItemRepository.getById(ItemId(1))
        } yield (assert(item)(isNone))
      },
      testM("get item 2") {
        for {
          item <- ItemRepository.getById(ItemId(2))
        } yield (assert(item)(isSome) && assert(item.get.description)(equalTo("second item")))
      },
      testM("update item 3") {
        for {
          _ <- ItemRepository.update(ItemId(3), Item(ItemId(3), "updated item"))
          item <- ItemRepository.getById(ItemId(3))
        } yield (assert(item)(isSome) && assert(item.get.description)(equalTo("updated item")))
      },
      
    ).provideCustomLayerShared(testLayer.orDie) @@ sequential
