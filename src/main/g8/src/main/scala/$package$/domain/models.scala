package $package$.domain

$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
final case class ItemId(value: Long)

final case class Item(id: ItemId, description: String)
$endif$

enum DomainError(val msg: String) extends Throwable:
  $if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
  case RepositoryError(cause: Throwable) extends DomainError(cause.getMessage)
  case BusinessError(message: String) extends DomainError(message)
  $endif$
  case ConfigError(e: Exception) extends DomainError(e.getMessage)
