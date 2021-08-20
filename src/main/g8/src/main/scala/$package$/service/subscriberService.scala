package com.example.service

import com.example.domain.ItemId
import zio.stream.*
import zio.*

trait SubscriberService:
  def publishDeleteEvents(deletedItemId: ItemId): IO[Nothing, List[Boolean]]
  def showDeleteEvents: Stream[Nothing, ItemId]

case class SubscriberServiceLive(deletedEventsSubscribers: Ref[List[Queue[ItemId]]])
    extends SubscriberService {
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

    def publishDeleteEvents(deletedItemId: ItemId): ZIO[Has[SubscriberService], Nothing, List[Boolean]] =
      ZIO.serviceWith[SubscriberService](_.publishDeleteEvents(deletedItemId))

    def showDeleteEvents: ZStream[Has[SubscriberService], Nothing, ItemId] =
      ZStream.accessStream(_.get.showDeleteEvents)
}
