package $package$.repo

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.{ IO, URLayer, ZIO, ZLayer }
import $package$.domain._

import java.sql.SQLException
import javax.sql.DataSource

final class ItemRepositoryLive(quill: Quill.Postgres[PluralizedTableNames]) extends ItemRepository:

  import quill.*

  inline def items = quote {
    querySchema[Item]("items")
  }

  override def add(description: String): IO[RepositoryError, ItemId] =
    val effect: IO[SQLException, ItemId] = run {
      quote {
        items
          .insertValue(lift(Item(ItemId(0), description)))
          .returningGenerated(_.id)
      }
    }

    effect
      .either
      .resurrect
      .refineOrDie {
        case e: NullPointerException => RepositoryError(e)
      }
      .flatMap {
        case Left(e: SQLException) => ZIO.fail(RepositoryError(e))
        case Right(itemId: ItemId) => ZIO.succeed(itemId)
      }

  def delete(id: ItemId): IO[RepositoryError, Long] =
    run(quote(items.filter(i => i.id == lift(id)).delete))
      .mapError(RepositoryError(_))

  def getAll(): IO[RepositoryError, List[Item]] =
    run(quote {
      items
    })
      .mapError(RepositoryError(_))

  def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    run(quote {
      items.filter(_.id == lift(id))
    })
      .map(_.headOption)
      .mapError(RepositoryError(_))

  def update(item: Item): IO[RepositoryError, Option[Unit]] =
    run(quote {
      items
        .filter(i => i.id == lift(item.id))
        .update(_.description -> lift(item.description))
    })
      .map(n => if (n > 0) Some(()) else None)
      .refineOrDie {
        case e: SQLException => RepositoryError(e)
      }

object ItemRepositoryLive:

  val layer: URLayer[Quill.Postgres[PluralizedTableNames], ItemRepository] = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[PluralizedTableNames]]
    } yield ItemRepositoryLive(quill)
  }
