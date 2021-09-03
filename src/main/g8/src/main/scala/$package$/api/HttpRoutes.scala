package $package$.api

import zhttp.http._
import zhttp.service._
import zio._
import zio.json._
import zhttp.http.ResponseHelpers
import $package$.api.protocol._
import $package$.service.ItemService
import $package$.service.ItemService._

object HttpRoutes:

  val app: HttpApp[Has[ItemService], Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "items" =>
      getAllItems().map(items =>
        Response.jsonString(
          GetItems(items.map(item => GetItem(item.id.value, item.description))).toJson
        )
      )

    case Method.GET -> Root / "items" / id =>
      getItemById(id)
        .some
        .tapError {
          case Some(exception) => ZIO.unit
          case None            => ZIO.unit //TODO log error Item with id does not exist
        }
        .either
        .map {
          case Right(item) => Response.jsonString(GetItem(item.id.value, item.description).toJson)
          case Left(_)     => Response.status(Status.NOT_FOUND)
        }

    case Method.DELETE -> Root / "items" / id =>
      deleteItem(id)
        .tapError(e => ZIO.unit //TODO log
        )
        .either
        .map {
          case Right(_) => Response.ok
          case Left(_)  => Response.status(Status.NOT_FOUND)
        }

    case req @ Method.POST -> Root / "items" =>
      (for
        body <- ZIO
          .fromOption(req.getBodyAsString)
          .map(_.fromJson[CreateItem])
          .absolve
          .tapError(_ => ZIO.unit) //TODO log Unparseable body
        id <- addItem(body.description)
      yield GetItem(id.value, body.description)).either.map {
        case Right(created) =>
          Response.http(
            Status.CREATED,
            List(Header.contentTypeJson),
            HttpData.CompleteData(Chunk.fromArray(created.toJson.getBytes(HTTP_CHARSET))),
          )
        case Left(_) => Response.status(Status.BAD_REQUEST)
      }

    case req @ Method.PUT -> Root / "items" / id =>
      (for
        update <- ZIO
          .fromOption(req.getBodyAsString)
          .map(_.fromJson[UpdateItem])
          .absolve
          .tapError(_ => ZIO.unit) //TODO log Unparseable body
        _ <- updateItem(id, update.description)
      yield ()).either.map {
        case Left(_)  => Response.status(Status.BAD_REQUEST)
        case Right(_) => Response.ok
      }
  }
