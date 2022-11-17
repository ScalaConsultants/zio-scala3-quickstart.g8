package $package$.domain

sealed trait DomainError(message: String):

  def asThrowable: Throwable = this match
    case BusinessError(message) => Throwable(message)
    case RepositoryError(cause) => cause

final case class BusinessError(message: String)    extends DomainError(message)
final case class RepositoryError(cause: Throwable) extends DomainError(message = cause.getMessage)
