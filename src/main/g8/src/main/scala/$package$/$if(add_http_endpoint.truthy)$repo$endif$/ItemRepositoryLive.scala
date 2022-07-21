package $package$.repo

import io.getquill._
import io.getquill.context.ZioJdbc._
import io.getquill.context.Context
import io.getquill.context.qzio.ZioJdbcContext
import zio.{ IO, ZLayer, ZIO }
import $package$.domain._

import javax.sql.DataSource

final class ItemRepositoryLive(
    ds: DataSource,
    ctx: PostgresZioJdbcContext[PluralizedTableNames])
    extends ItemRepository:

  private val dsLayer = ZLayer(ZIO.succeed(ds))

  import ctx._

  inline def items = quote {
    querySchema[Item]("items", _.id.value -> "id", _.description -> "description")
  }

  // TODO return generated ID with the use of "returningGenerated" method
  // issue opened https://github.com/getquill/protoquill/issues/22
  def add(description: String): IO[RepositoryError, ItemId] =
    ctx
      .transaction(for {
        _ <- ctx.run(
          quote {
            items.insert(_.description -> lift(description)).returning(_.id)
          }
        )
        result <- ctx.run(
          quote {
            items.filter(_.description == lift(description)).map(_.id)
          }
        )
        generatedId = result.headOption.fold(0L)(_.value)
      } yield ItemId(generatedId))
      .mapError(e => RepositoryError(e))
      .provide(dsLayer)

  def delete(id: ItemId): IO[RepositoryError, Unit] =
    ctx
      .run(quote(items.filter(i => i.id == lift(id)).delete))
      .mapError(e => new RepositoryError(e))
      .provide(dsLayer)
      .unit

  def getAll(): IO[RepositoryError, List[Item]] =
    ctx
      .run(quote {
        items
      })
      .provide(dsLayer)
      .mapError(e => new RepositoryError(e))

  def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    ctx
      .run(quote {
        items.filter(_.id == lift(id))
      })
      .map(_.headOption)
      .mapError(e => new RepositoryError(e))
      .provide(dsLayer)

  def update(item: Item): IO[RepositoryError, Unit] =
    ctx
      .run(quote {
        items
          .filter(i => i.id == lift(item.id))
          .update(_.description -> lift(item.description))
      })
      .mapError(e => new RepositoryError(e))
      .provide(dsLayer)
      .unit

object ItemRepositoryLive:

  val layer: ZLayer[DataSource, Nothing, ItemRepository] =
    ZLayer(
      for ds <- ZIO.service[DataSource]
      yield ItemRepositoryLive(
        ds,
        new PostgresZioJdbcContext(PluralizedTableNames),
      )
    )
