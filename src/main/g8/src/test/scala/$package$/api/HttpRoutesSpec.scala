package $package$.api

import zio.test._
import zio.test.Assertion
import zio.test.Assertion._
import zhttp.test._
import zhttp.http._
import $package$.util._
import zio.test.assertM
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import $package$.Main
import zio.test.TestAspect._
import zio.random._
import zio.console._
import zio.json._
import $package$.repo.itemrepository._
import $package$.service.itemservice._
import zio.test.mock._
import zhttp.test.HttpWithTest
import zio.test.Assertion._
import zio.test.mock.Expectation._
import zio.test.mock.MockRandom
import $package$.api.protocol._
import zhttp.http.HttpData.StreamData
import $package$.domain._

object HttpRoutesSpec extends HttpRunnableSpec(8082):

  private val env = EventLoopGroup.auto() ++ ChannelFactory.auto ++ ServerChannelFactory.auto

  private val firstItem = "first description"
  private val firstItemId = firstItem.hashCode.abs
  private val updatedFirst = "new description"
  private val secondItem = "second description"
  private val secondItemId = secondItem.hashCode.abs
  private val thirdItem = "third description"
  private val thirdItemId = thirdItem.hashCode.abs

//  val mockRandomEnv: ULayer[Random] = MockRandom.NextLong(value(firstItemId))
  //  private val repoLayer  = (Console.live ++ Random.live) >>> ItemRepo.live
  //  private val businessLayer = repoLayer >>> BusinessLogic.live

  val app = serve(HttpRoutes.app)

  def spec = (suiteM("http routes")(
    app
      .as(
        List(
          testM("create item") {
            val status1 = request(Root / "item", Method.POST, s"{\"description\": \"\$firstItem\"}")
              .map(_.status)
            val status2 = request(Root / "item", Method.POST, s"{\"description\": \"\$secondItem\"}")
              .map(_.status)
            val status3 = request(Root / "item", Method.POST, s"{\"description\": \"\$thirdItem\"}")
              .map(_.status)
            for {
              res1 <- assertM(status1)(equalTo(Status.CREATED))
              res2 <- assertM(status2)(equalTo(Status.CREATED))
              res3 <- assertM(status3)(equalTo(Status.CREATED))
            } yield (res1 && res2 && res3)
          }
        )
      )
      .useNow
  )).provideCustomLayerShared(env ++ testLayer)

  def getBodyAsString(body: HttpData[Any, Nothing]): IO[Throwable, String] = body match {
    case HttpData.CompleteData(data) => ZIO.succeed(data.map(_.toChar).mkString)
    case _                           => ZIO.fail(new RuntimeException("unexpected content"))
  }

  private val testLayer: ZLayer[Any, Nothing, BusinessLogic] =
    ZLayer.succeed(new BusinessLogic.Service {
      private val items: Map[String, String] = Map.empty

      def addItem(description: String): IO[DomainError, ItemId] = IO.succeed(items + (description.hashCode.abs.toString -> description)) *> ZIO.succeed(ItemId(description.hashCode.abs))

      def deleteItem(id: String): IO[DomainError, Unit] = ZIO.succeed(items.removed(id))

      def getAllItems(): IO[DomainError, List[Item]] = ZIO.succeed(
        items
          .view
          .map {
            case (key, value) => Item(ItemId(key.toLong), value)
          }
          .toList
      )

      def getItemById(id: String): IO[DomainError, Option[Item]] =
        ZIO.succeed(items.get(id).map(desc => Item(ItemId(id.toLong), desc)))

      def getItemsByIds(ids: Set[String]): IO[DomainError, List[Item]] =
        ZIO.succeed(
          ids.flatMap(id => items.get(id).map(value => Item(ItemId(id.toLong), value))).toList
        )

      def updateItem(id: String, description: String): IO[DomainError, Unit] =
        items.get(id) match {
          case Some(value) =>
            items + (id -> description)
            ZIO.unit
          case None => ZIO.fail(DomainError.BusinessError(s"no such key \$id"))
        }
    })
