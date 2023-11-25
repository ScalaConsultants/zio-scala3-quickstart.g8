package $package$.api.healthcheck

import zio._

trait HealthCheckService:
  def check: UIO[DbStatus]

object HealthCheckService:

  def check: URIO[HealthCheckService, DbStatus] = ZIO.serviceWithZIO(_.check)
