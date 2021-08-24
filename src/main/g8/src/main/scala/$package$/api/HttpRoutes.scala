package $package$.api

import zhttp.http._
import zhttp.service._
import zio._
import zio.json._
import $package$.api.protocol._
import $package$.service.BusinessLogicService
import $package$.service.BusinessLogicService._

object HttpRoutes:

  val app: HttpApp[Has[BusinessLogicService], Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "items" =>
      getAllItems().map(items =>
        Response.jsonString(
          GetItems(items.map(item => GetItem(item.id.value, item.description))).toJson
        )
      )

    case Method.GET -> Root / "item" / id =>
      getItemById(id)
        .some
        .mapError {
          case Some(exception) => exception
          case None            => new java.lang.RuntimeException(s"Item with \$id does not exists")
        }
        .map(item => Response.jsonString(GetItem(item.id.value, item.description).toJson))

    case Method.DELETE -> Root / "item" / id =>
      deleteItem(id).map(_ => Response.ok)

    case req @ Method.POST -> Root / "item" =>
      (for
        body <- ZIO
          .fromEither(req.getBodyAsString match {
            case Some(value) => value.fromJson[CreateItem]
            case None        => Left("Unparseable body")
          })
          .mapError(msg => new IllegalArgumentException(msg))
        id <- addItem(body.description)
       yield GetItem(id.value, body.description)).map(created =>
        Response.http(
          Status.CREATED,
          List(Header.contentTypeJson),
          HttpData.CompleteData(Chunk.fromArray(created.toJson.getBytes(HTTP_CHARSET))),
        )
      )

    case req @ Method.POST -> Root / "item" / "update" =>
      for
        update <- ZIO
          .fromEither(req.getBodyAsString match {
            case Some(value) => value.fromJson[UpdateItem]
            case None        => Left("Unparseable body")
          })
          .mapError(msg => new IllegalArgumentException(msg))
        _ <- updateItem(update.id, update.description)
      yield (Response.ok)

    case req @ Method.GET -> Root / "items" / "by-ids" =>
      (for
        itemIds <- ZIO
          .fromEither(req.getBodyAsString match {
            case Some(value) => value.fromJson[GetItemIds]
            case None        => Left("Unparseable body")
          })
          .mapError(msg => new IllegalArgumentException(msg))
        items <- getItemsByIds(itemIds.ids)
      yield items).map(items =>
        Response.jsonString(GetItems(items.map(i => GetItem(i.id.value, i.description))).toJson)
      )
  }
