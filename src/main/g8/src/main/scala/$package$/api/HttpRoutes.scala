package $package$.api

import zhttp.http._
import zhttp.service._
import zio._
import zio.json._
import zhttp.http.ResponseHelpers
import zio.zmx.metrics._
import $package$.api.protocol._
import $package$.service.ItemService
import $package$.service.ItemService._

object HttpRoutes:

  val app: HttpApp[Has[ItemService] with Has[Logger[String]], Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "items" =>
      getAllItems().map(items =>
        Response.jsonString(
          GetItems(items.map(item => GetItem(item.id.value, item.description))).toJson
        )
      ) @@ addCounter("get_items_counter") @@ addDurationMetric("get_items_duration")

    case Method.GET -> Root / "items" / id =>
      (getItemById(id)
        .some
        .tapError {
          case Some(exception) => 
            log.info(s"Exception occured when getting item for id \$id \${exception.msg}")
          case None => log.info(s"No item with id \$id exists.")
        } @@ addCounter("get_item_counter", Some(id)))
        .either
        .map {
          case Right(item) => Response.jsonString(GetItem(item.id.value, item.description).toJson)
          case Left(_)     => Response.status(Status.NOT_FOUND)
        } @@ addDurationMetric("get_item_duration")

    case Method.DELETE -> Root / "items" / id =>
      deleteItem(id)
        .tapError(e => log.info(s"Error occured when deleting item with id \$id, \${e.msg}"))
        .either
        .map {
          case Right(_) => Response.ok
          case Left(_)  => Response.status(Status.NOT_FOUND)
        } @@ addCounter("delete_item_counter", Some(id)) @@ addDurationMetric(
        "delete_item_duration"
      )

    case req @ Method.POST -> Root / "items" =>
      (for
        body <- ZIO
          .fromOption(req.getBodyAsString)
          .map(_.fromJson[CreateItem])
          .absolve
          .tapError(_ => log.info(s"Unparseable body \${req.getBodyAsString}"))
        id <- addItem(body.description)
      yield GetItem(id.value, body.description)).either.map {
        case Right(created) =>
          Response.http(
            Status.CREATED,
            List(Header.contentTypeJson),
            HttpData.CompleteData(Chunk.fromArray(created.toJson.getBytes(HTTP_CHARSET))),
          )
        case Left(_) => Response.status(Status.BAD_REQUEST)
      } @@ addCounter("create_item_counter") @@ addDurationMetric("create_item_duration")

    case req @ Method.PUT -> Root / "items" / id =>
      (for
        update <- ZIO
          .fromOption(req.getBodyAsString)
          .map(_.fromJson[UpdateItem])
          .absolve
          .tapError(_ => log.info(s"Unparseable body \${req.getBodyAsString}"))
        _ <- updateItem(id, update.description) @@ addCounter("update_item", Some(id))
      yield ()).either.map {
        case Left(_)  => Response.status(Status.BAD_REQUEST)
        case Right(_) => Response.ok
      } @@ addDurationMetric("update_item_duration")
  }

  private def addDurationMetric(metricName: String): MetricAspect[Any] =
    MetricAspect.observeDurations(
      metricName,
      defaultBuckets,
    )(d => d.toMillis.toDouble)

  private def addCounter(metricName: String, optionId: Option[String] = None): MetricAspect[Any] =
    optionId match
      case Some(id) => MetricAspect.count(metricName, "item_id" -> id)
      case None     => MetricAspect.count(metricName)

  private val defaultBuckets =
    Chunk(5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500, 5000, 7500, 10000).map(_.toDouble)