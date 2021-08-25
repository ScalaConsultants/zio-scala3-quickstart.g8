package com.example.api

import zio.test._
import zhttp.test._
import zhttp.http._
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zio.random._
import zio.test.TestAspect._
import zio.console._
import zio.test.mock._
import zio.test.Assertion._
import zhttp.socket._
import $package$.Main
import $package$.repo._
import $package$.service._
import $package$.api.protocol._
import $package$.util._
import $package$.domain._

object WebSocketRouteSpec extends DefaultRunnableSpec {
  private val env = EventLoopGroup.auto() ++ ChannelFactory.auto ++ ServerChannelFactory.auto
  val repoLayer = (Console.live ++ Random.live) >>> ItemRepositoryLive.layer
  val subscriberLayer = ZLayer.fromEffect(Ref.make(List.empty)) >>> SubscriberServiceLive.layer
  val businessLayer = (repoLayer ++ subscriberLayer) >>> BusinessLogicServiceLive.layer
  val socketOpen = SocketApp.open(Socket.succeed(WebSocketFrame.text("ws openned!")))
  def spec =
    suite("ws check")(testM("a SocketResponse is returned") {
      for {
        response <- WebSocketRoute.socketImpl(Request(Method.GET -> URL(Root / "ws" / "items")))
        checkResponse = response match {
          case Response.SocketResponse(socket) =>
            val socketConfig = socket.config
            val openResult = socketConfig.onOpen.map(_ == socketOpen)
            openResult.getOrElse(false)
          case _ => false
        }
        result <- assertM(ZIO.succeed(checkResponse))(equalTo(true))
      } yield result
    }.provideCustomLayer(businessLayer ++ env))
}
