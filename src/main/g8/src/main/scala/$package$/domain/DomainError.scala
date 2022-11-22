package $package$.domain

sealed trait DomainError(message: String):

  def asThrowable: Throwable = this match
    case RepositoryError(cause)   => cause
    case ValidationError(message) => Throwable(message)
    case NotFoundError            => Throwable(message)

final case class RepositoryError(cause: Throwable) extends DomainError(message = cause.getMessage)
final case class ValidationError(message: String)  extends DomainError(message)
case object NotFoundError                          extends DomainError("NotFoundError")
