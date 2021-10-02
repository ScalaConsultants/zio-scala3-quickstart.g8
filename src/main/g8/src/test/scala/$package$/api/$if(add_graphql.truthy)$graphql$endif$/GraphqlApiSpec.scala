package $package$.api.graphql

import zio._
import zio.console._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.clock.Clock
import scala.tools.nsc.doc.html.HtmlTags.P
import zio.json._
import zio.stream._
import caliban.CalibanError
import caliban.ResponseValue
import $package$.service._
import $package$.domain._
import $package$.repo.InMemoryItemRepository

object GraphqlApiSpec extends DefaultRunnableSpec:

  import TestDecoders._

  val firstItem = Item(ItemId("first item".hashCode.abs.toLong), "first item")
  val secondItem = Item(ItemId("second item".hashCode.abs.toLong), "second item")
  val thirdItem = Item(ItemId("third item".hashCode.abs.toLong), "third item")

  override def spec = suite("graphql test suite")(
    testM("save items") {
      for
        interpreter <- GraphqlApi.api.interpreter
        _ <- interpreter.execute(Mutations.save(firstItem.description))
        _ <- interpreter.execute(Mutations.save(secondItem.description))
        _ <- interpreter.execute(Mutations.save(thirdItem.description))
        items <- ZIO.service[ItemService].flatMap(_.getAllItems())
      yield assert(items)(hasSize(equalTo(3))) &&
      assert(items(0).description)(equalTo("first item")) &&
      assert(items(1).description)(equalTo("second item")) &&
      assert(items(2).description)(equalTo("third item"))
    },
    testM("get item by id") {
      for
        interpreter <- GraphqlApi.api.interpreter
        response <- interpreter.execute(Queries.getById(firstItem.id.value.toString))
        item <- ZIO.fromEither(response.data.toString.fromJson[ItemWrapper])
      yield assert(response.errors)(hasSize(equalTo(0))) && assert(item.getItem.description)(
        equalTo(firstItem.description)
      )
    },
    testM("update item") {
      for
        interpreter <- GraphqlApi.api.interpreter
        id = secondItem.id.value.toString
        _ <- interpreter.execute(Mutations.update(id, "new description"))
        updated <- ZIO.service[ItemService].flatMap(_.getItemById(id))
      yield assert(updated.get.description)(equalTo("new description"))
    },
    testM("delete item") {
      for
        interpreter <- GraphqlApi.api.interpreter
        id = firstItem.id.value.toString
        exist <- ZIO.service[ItemService].flatMap(_.getItemById(id))
        _ <- interpreter.execute(Mutations.delete(id))
        removed <- ZIO.service[ItemService].flatMap(_.getItemById(id))
      yield assert(exist)(isSome(equalTo(firstItem))) && assert(removed)(isNone)
    },
    testM("subscribe to stream of all deleted items") {
      for
        interpreter <- GraphqlApi.api.interpreter
        firstId = firstItem.id.value.toString
        secondId = secondItem.id.value.toString
        thirdId = thirdItem.id.value.toString
        _ <- interpreter.execute(Mutations.delete(secondId))
        _ <- interpreter.execute(Mutations.delete(thirdId))
        stream <- interpreter
          .execute(Subscriptions.streamDeleted)
          .map(_.data)
          .collect(CalibanError.ExecutionError("unexpected error")) {
            case ResponseValue.ObjectValue(list) =>
              list(0) match
                case (_, ResponseValue.StreamValue(stream)) => stream
                case _                                      => ZStream.empty
          }

        itemIds <- stream
          .map(_.toString.fromJson[ItemId])
          .right
          .take(3)
          .runCollect
          .map(_.toList)
      yield assert(itemIds)(hasSize(equalTo(3))) &&
      assert(itemIds(0).value.toString)(equalTo(firstId)) &&
      assert(itemIds(1).value.toString)(equalTo(secondId)) &&
      assert(itemIds(2).value.toString)(equalTo(thirdId))

    },
  ).provideCustomLayerShared(ItemServiceSpec.testLayer) @@ sequential

  final case class ItemWrapper(getItem: Item)

  object TestDecoders:
    implicit val wrapperDecoder: JsonDecoder[ItemWrapper] = DeriveJsonDecoder.gen[ItemWrapper]
    implicit val itemIdDecoder: JsonDecoder[ItemId] = DeriveJsonDecoder.gen[ItemId]
    implicit val itemDecoder: JsonDecoder[Item] = DeriveJsonDecoder.gen[Item]
