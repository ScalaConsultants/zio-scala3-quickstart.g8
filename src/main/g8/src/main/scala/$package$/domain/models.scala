package $package$.domain

final case class ItemId(value: Long) extends AnyVal

final case class Item(id: ItemId, description: String)

enum DomainError(val msg: String):
  case BusinessError(message: String) extends DomainError(message)

final case class DbStatus(status: Boolean)
