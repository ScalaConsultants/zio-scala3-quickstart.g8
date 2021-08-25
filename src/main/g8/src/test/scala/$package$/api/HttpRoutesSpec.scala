package $package$.api

import zio.test._
import zhttp.test._
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.random._
import zio.test.TestAspect._
import zio.console._
import zio.json._
import zio.test.mock._
import zhttp.test.HttpWithTest
import zio.test.Assertion._
import zio.test.mock.Expectation._
import zio.test.mock.MockRandom
import zhttp.http.HttpData.StreamData
import $package$.Main
import $package$.repo._
import $package$.service._
import $package$.api.protocol._
import $package$.util._
import $package$.domain._

object HttpRoutesSpec extends HttpRunnableSpec(8082):

  private val env = EventLoopGroup.auto() ++ ChannelFactory.auto ++ ServerChannelFactory.auto

  private val firstItem = "first description"
  private val firstItemId = 12345L
  private val updatedFirst = "new description"
  private val secondItem = "second description"
  private val secondItemId = 23456L
  private val thirdItem = "third description"
  private val thirdItemId = 5678L

  private val originItems = GetItems(
    List(
      GetItem(firstItemId, firstItem),
      GetItem(secondItemId, secondItem),
      GetItem(thirdItemId, thirdItem),
    )
  )
  private val updatedItems = GetItems(
    List(GetItem(firstItemId, updatedFirst), GetItem(thirdItemId, thirdItem))
  )
  private val onlyThird = GetItems(List(GetItem(thirdItemId, thirdItem)))

  val mockRandomEnv: ULayer[Random] =
    MockRandom.NextLong(value(firstItemId)) ++ MockRandom.NextLong(
      value(secondItemId)
    ) ++ MockRandom.NextLong(
      value(thirdItemId)
    )
  val repoLayer = (Console.live ++ mockRandomEnv) >>> ItemRepositoryLive.layer
  $if(add_websocket_endpoint.truthy)$
  private val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  $endif$
  val businessLayer = repoLayer $if(add_websocket_endpoint.truthy)$ ++ subscriberLayer $endif$ >>> BusinessLogicServiceLive.layer

  val app = serve(HttpRoutes.app)

  def spec = (suiteM("http routes")(
    app
      .as(
        List(
          //TODO move to integration tests ?
          testM("end to end test") {
            val status1 = request(Root / "item", Method.POST, s"{\"description\": \"\$firstItem\"}")
              .map(_.status)
            val status2 = request(Root / "item", Method.POST, s"{\"description\": \"\$secondItem\"}")
              .map(_.status)
            val status3 = request(Root / "item", Method.POST, s"{\"description\": \"\$thirdItem\"}")
              .map(_.status)
            val getAll = request(Root / "items", Method.GET, "")
              .flatMap(res => getBodyAsString(res.content))
            val updateStatus = request(
              Root / "item" / "update",
              Method.POST,
              UpdateItem(firstItemId.toString, updatedFirst).toJson,
            ).map(_.status)
            val getSecondItem = request(Root / "item" / secondItemId.toString, Method.GET, "")
              .flatMap(res => getBodyAsString(res.content))
            val deleteSecondItem =
              request(Root / "item" / secondItemId.toString, Method.DELETE, "").map(_.status)
            val getOthers = request(Root / "items", Method.GET, "")
              .flatMap(res => getBodyAsString(res.content))
            val getLast = request(
              Root / "items" / "by-ids",
              Method.GET,
              GetItemIds(Set(thirdItemId.toString)).toJson,
            )
              .flatMap(res => getBodyAsString(res.content))
            for
              create1 <- assertM(status1)(equalTo(Status.CREATED))
              create2 <- assertM(status2)(equalTo(Status.CREATED))
              create3 <- assertM(status3)(equalTo(Status.CREATED))
              getAll1 <- assertM(getAll)(equalTo(originItems.toJson))
              updateFirst <- assertM(updateStatus)(equalTo(Status.OK))
              getSecond <- assertM(getSecondItem)(equalTo(GetItem(secondItemId, secondItem).toJson))
              deleteSecond <- assertM(deleteSecondItem)(equalTo(Status.OK))
              getRest <- assertM(getOthers)(equalTo(updatedItems.toJson))
              getThird <- assertM(getLast)(equalTo(onlyThird.toJson))
            yield (create1 && create2 && create3 && getAll1 && updateFirst &&
            getSecond && deleteSecond && getRest && getThird)
          }
        )
      )
      .useNow
  )).provideCustomLayerShared(businessLayer ++ env)

  def getBodyAsString(body: HttpData[Any, Nothing]): IO[Throwable, String] = body match {
    case HttpData.CompleteData(data) => ZIO.succeed(data.map(_.toChar).mkString)
    case _                           => ZIO.fail(new RuntimeException("unexpected content"))
  }
