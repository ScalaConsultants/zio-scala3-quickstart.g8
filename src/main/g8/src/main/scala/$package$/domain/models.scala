package $package$.domain

final case class ItemId(value: Long) extends AnyVal

final case class Item(id: ItemId, description: String)
