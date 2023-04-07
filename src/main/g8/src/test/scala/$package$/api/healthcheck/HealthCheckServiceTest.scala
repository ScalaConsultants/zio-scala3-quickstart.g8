package $package$.api.healthcheck

import zio._

final class HealthCheckServiceTest extends HealthCheckService:

  override def check: UIO[DbStatus] = ZIO.succeed(DbStatus(true))

object HealthCheckServiceTest:

  val layer: ULayer[HealthCheckServiceTest] = ZLayer {
    ZIO.succeed(HealthCheckServiceTest())
  }
