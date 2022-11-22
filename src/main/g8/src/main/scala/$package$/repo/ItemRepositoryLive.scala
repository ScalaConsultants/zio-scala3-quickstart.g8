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

  // TODO return generated ID with the use of "returningGenerated" method
  // issue opened https://github.com/getquill/protoquill/issues/22
  def add(description: String): IO[RepositoryError, ItemId] =
    transaction {
      for {
        _          <- run(
                        quote {
                          items.insert(_.description -> lift(description)).returning(_.id)
                        }
                      )
        result     <- run(
                        quote {
                          items.filter(_.description == lift(description)).map(_.id)
                        }
                      )
        generatedId = result.headOption.fold(0L)(_.value)
      } yield ItemId(generatedId)
    }
      .mapError(RepositoryError(_))

  def delete(id: ItemId): IO[RepositoryError, Unit] =
    run(quote(items.filter(i => i.id == lift(id)).delete))
      .mapError(RepositoryError(_))
      .unit

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
