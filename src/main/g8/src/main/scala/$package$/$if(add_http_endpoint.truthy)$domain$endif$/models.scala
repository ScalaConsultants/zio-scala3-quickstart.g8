package $package$.domain

final case class ItemId(value: Long)

final case class Item(id: ItemId, description: String)

enum DomainError(val msg: String):
  case BusinessError(message: String) extends DomainError(message)
