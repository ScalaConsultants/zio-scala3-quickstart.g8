package com.example.service

import $package$.domain.ItemId
import zio.*
import zio.stream.*

final case class SubscriberServiceLive(deletedEventsSubscribers: Ref[List[Queue[ItemId]]])
    extends SubscriberService {
  override def getQueue: UIO[List[Queue[ItemId]]] = deletedEventsSubscribers.get
  override def publishDeleteEvents(deletedItemId: ItemId): IO[Nothing, List[Boolean]] =
    deletedEventsSubscribers.get.flatMap { subs =>
      UIO.foreach(subs) { queue =>
        queue
          .offer(deletedItemId)
          .onInterrupt(
            deletedEventsSubscribers.update(_.filterNot(_ == queue))
          )
      }
    }
  override def showDeleteEvents: Stream[Nothing, ItemId] = ZStream.unwrap {
    for {
      queue <- Queue.unbounded[ItemId]
      _ <- deletedEventsSubscribers.update(queue :: _)
    } yield ZStream.fromQueue(queue)
  }
}

object SubscriberServiceLive {
  val layer: URLayer[Has[Ref[List[Queue[ItemId]]]], Has[SubscriberService]] =
    (SubscriberServiceLive(_)).toLayer
}