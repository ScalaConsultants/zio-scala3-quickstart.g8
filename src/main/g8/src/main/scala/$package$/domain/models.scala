package $package$.domain

$if(add_http_endpoints_and_database_repositories.truthy)$
final case class ItemId(value: Long)


final case class Item(id: ItemId, description: String)
$endif$

enum DomainError(val msg: String) extends Throwable:
  $if(add_http_endpoints_and_database_repositories.truthy)$
  case RepositoryError(cause: Throwable) extends DomainError(cause.getMessage)
  case BusinessError(message: String) extends DomainError(message)
  $endif$
  case ConfigError(e: Exception) extends DomainError(e.getMessage)
