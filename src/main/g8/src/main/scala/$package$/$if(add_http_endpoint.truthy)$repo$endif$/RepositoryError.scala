package com.example.repo

case class RepositoryError(cause: Throwable) extends RuntimeException(cause)
