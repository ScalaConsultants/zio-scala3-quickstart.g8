package $package$.api

import zio.json._

object protocol:

  final case class UpdateItem(id: String, description: String)
  object UpdateItem:
    implicit val updateItemDecoder: JsonDecoder[UpdateItem] = DeriveJsonDecoder.gen[UpdateItem]
    implicit val updateItemEncoder: JsonEncoder[UpdateItem] = DeriveJsonEncoder.gen[UpdateItem]

  final case class CreateItem(description: String)  
  object CreateItem:
    implicit val createItemDecoder: JsonDecoder[CreateItem] = DeriveJsonDecoder.gen[CreateItem]

  final case class GetItems(items: List[GetItem])
  object GetItems:
    implicit val getItemsDecoder: JsonDecoder[GetItems] = DeriveJsonDecoder.gen[GetItems]
    implicit val getItemsEncoder: JsonEncoder[GetItems] = DeriveJsonEncoder.gen[GetItems]

  final case class GetItem(id: Long, description: String)
  object GetItem:
      implicit val itemCreteadDecoder: JsonDecoder[GetItem] = DeriveJsonDecoder.gen[GetItem]
      implicit val itemCreatedEncoder: JsonEncoder[GetItem] = DeriveJsonEncoder.gen[GetItem]

  final case class GetItemIds(ids: Set[String])
  object GetItemIds:
    implicit val itemIdsDecoder: JsonDecoder[GetItemIds] = DeriveJsonDecoder.gen[GetItemIds]
    implicit val itemIdsEncoder: JsonEncoder[GetItemIds] = DeriveJsonEncoder.gen[GetItemIds]

