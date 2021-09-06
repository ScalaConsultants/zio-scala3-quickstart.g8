package $package$.api

import zio.json._

object protocol:

  final case class UpdateItem(description: String)
  object UpdateItem:
    implicit val updateItemDecoder: JsonDecoder[UpdateItem] = DeriveJsonDecoder.gen[UpdateItem]

  final case class CreateItem(description: String)  
  object CreateItem:
    implicit val createItemDecoder: JsonDecoder[CreateItem] = DeriveJsonDecoder.gen[CreateItem]

  final case class GetItems(items: List[GetItem])
  object GetItems:
    implicit val getItemsEncoder: JsonEncoder[GetItems] = DeriveJsonEncoder.gen[GetItems]

  final case class GetItem(id: Long, description: String)
  object GetItem:
      implicit val itemCreatedEncoder: JsonEncoder[GetItem] = DeriveJsonEncoder.gen[GetItem]
