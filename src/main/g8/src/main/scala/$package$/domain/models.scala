package $package$.domain

final case class ItemId(value: Long) extends AnyVal

final case class Item(id: ItemId, description: String)

enum DomainError(val msg: String) extends Throwable:
  case RepositoryError(cause: Throwable) extends DomainError(cause.getMessage)
  case BusinessError(message: String) extends DomainError(message)
  case ConfigError(e: Exception) extends DomainError(e.getMessage)

final case class HeathCheckStatus(up: Boolean, msg: String)
