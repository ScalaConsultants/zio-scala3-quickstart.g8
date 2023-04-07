package $package$.api

import $package$.domain.{ Item, ItemId }
import zio.json._

final case class UpdateItemRequest(name: String, price: BigDecimal)
final case class PartialUpdateItemRequest(name: Option[String], price: Option[BigDecimal])
final case class CreateItemRequest(name: String, price: BigDecimal)

trait JsonSupport:
  implicit val itemIdEncoder: JsonEncoder[ItemId] = JsonEncoder[Long].contramap(_.value)
  implicit val itemEncoder: JsonEncoder[Item]     = DeriveJsonEncoder.gen[Item]

  implicit val updateItemDecoder: JsonDecoder[UpdateItemRequest] = DeriveJsonDecoder.gen[UpdateItemRequest]

  implicit val partialUpdateItemDecoder: JsonDecoder[PartialUpdateItemRequest] =
    DeriveJsonDecoder.gen[PartialUpdateItemRequest]

  implicit val createItemDecoder: JsonDecoder[CreateItemRequest] = DeriveJsonDecoder.gen[CreateItemRequest]

object JsonSupport extends JsonSupport