package $package$.repo

import zio.{IO, ZLayer, ZIO, Has}
import io.getquill._
import io.getquill.context.ZioJdbc._
import zio.blocking.Blocking
import zio.logging.Logging
import zio.logging.Logger
import java.sql.SQLException
import io.getquill.context.Context
import io.getquill.context.qzio.ZioJdbcContext
import $package$.domain._
import $package$.domain.DomainError.RepositoryError

final class ItemRepositoryLive(
    log: Logger[String],
    connection: QConnection,
    ctx: PostgresZioJdbcContext[PluralizedTableNames],
  ) extends ItemRepository:

  import ctx._

  // TODO return generated ID with the use of "returningGenerated" method
  // issue opened https://github.com/getquill/protoquill/issues/22
  def add(description: String): IO[RepositoryError, ItemId] =
    ctx
      .transaction(for {
        _ <- ctx.run(
          quote {
            query[Item].insert(_.description -> lift(description))
          }
        )
        result <- ctx.run(
          quote {
            query[Item].filter(_.description == lift(description)).map(_.id)
          }
        )
        generatedId = result.headOption.fold(0L)(_.value)
      } yield (ItemId(generatedId)))
      .mapError(e => new RepositoryError(e))
      .provide(connection)

  def delete(id: ItemId): IO[RepositoryError, Unit] =
    ctx
      .run(quote(query[Item].filter(i => i.id == lift(id)).delete))
      .mapError(e => new RepositoryError(e))
      .provide(connection)
      .unit

  def getAll(): IO[RepositoryError, List[Item]] =
    ctx
      .run(quote {
        query[Item]
      })
      .provide(connection)
      .mapError(e => new RepositoryError(e))

  def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    ctx
      .run(quote {
        query[Item].filter(_.id == lift(id))
      })
      .map(_.headOption)
      .mapError(e => new RepositoryError(e))
      .provide(connection)

  def update(itemId: ItemId, value: Item): IO[RepositoryError, Unit] =
    ctx
      .run(quote {
        query[Item]
          .filter(i => i.id == lift(itemId))
          .update(_.description -> lift(value.description))
      })
      .mapError(e => new RepositoryError(e))
      .provide(connection)
      .unit

object ItemRepositoryLive:

  val layer: ZLayer[Logging with QConnection, Nothing, Has[ItemRepository]] =
    (for {
      logging <- ZIO.service[Logger[String]]
      connection <- ZIO.environment[QConnection]
    } yield (ItemRepositoryLive(
      logging,
      connection,
      new PostgresZioJdbcContext(PluralizedTableNames),
    ))).toLayer
