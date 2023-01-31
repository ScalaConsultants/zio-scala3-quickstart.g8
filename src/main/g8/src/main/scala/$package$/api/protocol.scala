package $package$.api

import zio.json._

import $package$.domain.{ Item, ItemId }

final case class UpdateItemRequest(description: String)
final case class PartialUpdateItemRequest(description: Option[String])
final case class CreateItemRequest(description: String)

trait JsonSupport:
  implicit val itemIdEncoder: JsonEncoder[ItemId] = JsonEncoder[Long].contramap(_.value)
  implicit val itemEncoder: JsonEncoder[Item]     = DeriveJsonEncoder.gen[Item]

  implicit val updateItemDecoder: JsonDecoder[UpdateItemRequest] = DeriveJsonDecoder.gen[UpdateItemRequest]

  implicit val partialUpdateItemDecoder: JsonDecoder[PartialUpdateItemRequest] =
    DeriveJsonDecoder.gen[PartialUpdateItemRequest]

  implicit val createItemDecoder: JsonDecoder[CreateItemRequest] = DeriveJsonDecoder.gen[CreateItemRequest]

object JsonSupport extends JsonSupport