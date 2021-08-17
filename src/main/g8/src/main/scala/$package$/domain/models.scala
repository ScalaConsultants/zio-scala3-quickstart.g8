package $package$.domain

final case class ItemId(value: Long) extends AnyVal

final case class Item(id: ItemId, description: String)

sealed trait DomainError(val msg: String) extends Exception 

object DomainError:
  final case class RepositoryError(cause: Throwable) extends DomainError(cause.getMessage)
  final case class BusinessError(message: String)    extends DomainError(message)

final case class HeathCheckStatus(up: Boolean, msg: String)
