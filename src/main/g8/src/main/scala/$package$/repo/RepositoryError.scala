package $package$.repo

case class RepositoryError(cause: Throwable) extends RuntimeException(cause)
