package $package$.infrastructure

import io.getquill._
import io.getquill.jdbczio.Quill
import zio.{ IO, URLayer, ZIO, ZLayer }
import $package$.domain._

import java.sql.SQLException
import javax.sql.DataSource

final class ItemRepositoryLive(quill: Quill.Postgres[Literal]) extends ItemRepository:

  import quill.*

  inline def items = quote {
    querySchema[Item]("items")
  }

  override def add(data: ItemData): IO[RepositoryError, ItemId] =
    val effect: IO[SQLException, ItemId] = run {
      quote {
        items
          .insertValue(lift(Item.withData(ItemId(0), data)))
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

  override def delete(id: ItemId): IO[RepositoryError, Long] =
    val effect: IO[SQLException, Long] = run {
      quote {
        items.filter(i => i.id == lift(id)).delete
      }
    }

    effect.refineOrDie {
      case e: SQLException => RepositoryError(e)
    }

  override def getAll(): IO[RepositoryError, List[Item]] =
    val effect: IO[SQLException, List[Item]] = run {
      quote {
        items
      }
    }

    effect.refineOrDie {
      case e: SQLException => RepositoryError(e)
    }

  override def getById(id: ItemId): IO[RepositoryError, Option[Item]] =
    val effect: IO[SQLException, List[Item]] = run {
      quote {
        items.filter(_.id == lift(id))
      }
    }

    effect
      .map(_.headOption)
      .refineOrDie {
        case e: SQLException => RepositoryError(e)
      }

  override def update(itemId: ItemId, data: ItemData): IO[RepositoryError, Option[Unit]] =
    val effect: IO[SQLException, Long] = run {
      quote {
        items
          .filter(item => item.id == lift(itemId))
          .updateValue(lift(Item.withData(itemId, data)))
      }
    }

    effect
      .map(n => if (n > 0) Some(()) else None)
      .refineOrDie {
        case e: SQLException => RepositoryError(e)
      }

object ItemRepositoryLive:

  val layer: URLayer[Quill.Postgres[Literal], ItemRepository] = ZLayer {
    for {
      quill <- ZIO.service[Quill.Postgres[Literal]]
    } yield ItemRepositoryLive(quill)
  }
