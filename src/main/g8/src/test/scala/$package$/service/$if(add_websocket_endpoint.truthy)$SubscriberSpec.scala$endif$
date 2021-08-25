package com.example.service

import $package$.domain.ItemId
import $package$.service.*
import zio.test.DefaultRunnableSpec
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.stream.*
import zio.console.*
import zio.duration.*
import zio.test.environment.TestClock

object SubscriberSpec extends DefaultRunnableSpec {
  val deletedEventsSubscribersMock: UIO[Ref[List[Queue[ItemId]]]] =
    for {
      queue <- Queue.unbounded[ItemId]
      ref <- Ref.make(List.empty[Queue[ItemId]])
      _ <- ref.update(queue :: _)
    } yield ref
  val subscriberLayerPublish =
    ZLayer.fromEffect(deletedEventsSubscribersMock) >>> SubscriberServiceLive.layer
  val subscriberLayerShow =
    ZLayer.fromEffect(Ref.make(List.empty[Queue[ItemId]])) >>> SubscriberServiceLive.layer
  val itemId: ItemId = ItemId(123)

  def spec = suite("subscriber service -  publishDeleteEvents test")(
    testM("get a list containing the published confirmation") {
      for {
        published <- assertM(SubscriberService.publishDeleteEvents(itemId))(
          equalTo(List(true))
        )
      } yield published
    }.provideCustomLayer(subscriberLayerPublish),
    suite("subscriber service -  showDeleteEvents test")(
      testM("add itemId queue to Ref") {
        for {
          fiber <- SubscriberService
            .showDeleteEvents
            .haltAfter(5.seconds)
            .runCollect
            .fork
          _ <- TestClock.adjust(6.seconds)
          _ <- SubscriberService.publishDeleteEvents(itemId)
          value <- SubscriberService.getQueue
          _ <- fiber.join
          res <- assertM(ZIO.succeed(value.length))(equalTo(1))
        } yield res
      }.provideCustomLayer(subscriberLayerShow)
    ),
  )
}
