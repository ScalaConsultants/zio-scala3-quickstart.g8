package $package$.service

import zio._

import $package$.domain.DbStatus

trait HealthCheckService:
  def check: UIO[DbStatus]

object HealthCheckService:

  def check: URIO[HealthCheckService, DbStatus] = ZIO.serviceWithZIO(_.check)
