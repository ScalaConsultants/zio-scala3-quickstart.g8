package $package$.domain

final case class ItemId(value: Long) extends AnyVal

final case class Item(id: ItemId, description: String)

sealed trait DomainError

object DomainError:
  final case class RepositoryError(cause: Throwable) extends DomainError
  final case class BusinessError(msg: String)        extends DomainError

final case class HeathCheckStatus(up: Boolean, msg: String)
