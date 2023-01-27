package $package$.service

import io.getquill.*
import io.getquill.PluralizedTableNames
import io.getquill.jdbczio.Quill

import zio.*

import $package$.domain.DbStatus

final class HealthCheckServiceLive(quill: Quill.Postgres[PluralizedTableNames]) extends HealthCheckService {

  import quill.*

  override def check: UIO[DbStatus] = run {
    quote {
      sql"""SELECT 1""".as[Query[Int]]
    }
  }
    .fold(
      _ => DbStatus(false),
      _ => DbStatus(true),
    )

}

object HealthCheckServiceLive:

  val layer: URLayer[Quill.Postgres[PluralizedTableNames], HealthCheckServiceLive] = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[PluralizedTableNames]]
    } yield HealthCheckServiceLive(quill)
  }
