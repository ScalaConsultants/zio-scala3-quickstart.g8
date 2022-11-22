package $package$.domain

sealed trait DomainError(message: String)

final case class RepositoryError(cause: Throwable) extends DomainError(message = cause.getMessage)
final case class ValidationError(message: String)  extends DomainError(message)
case object NotFoundError                          extends DomainError("NotFoundError")
