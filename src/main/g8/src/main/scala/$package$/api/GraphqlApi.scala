package $package$.api

import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.schema.Annotations.{ GQLDeprecated, GQLDescription }
import caliban.schema.{ ArgBuilder, GenericSchema, Schema }
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._
import $package$.domain._
import $package$.service.ItemService
import $package$.service.ItemService._
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio._

import scala.language.postfixOps

object GraphqlApi extends GenericSchema[Has[ItemService]] {
  implicit val itemIdSchema: Schema[Any, ItemId] =
    Schema.longSchema.contramap[ItemId](_.value)
  implicit val itemIdArgBuilder: ArgBuilder[ItemId] = ArgBuilder.long.map(ItemId(_))
  implicit val itemSchema: Schema[Any, Item] = Schema.gen[Item]

  final private case class ItemInput(description: String)

  final private case class Queries(
      @GQLDescription("Get all items")
      allItems: ZIO[Has[ItemService], DomainError, List[Item]],
      @GQLDescription("Get item by ID")
      item: ItemId => ZIO[Has[ItemService], DomainError, Option[Item]],
    )
  final private case class Mutations(
      addItem: ItemInput => ZIO[Has[ItemService], DomainError, ItemId],
      deleteItem: ItemId => ZIO[Has[ItemService], DomainError, Unit],
    )

  val api: GraphQL[Console with Clock with Has[ItemService]] =
    graphQL(
      RootResolver(
        Queries(
          ItemService.getAllItems(),
          id => ItemService.getItemById(id.value.toString),
        ),
        Mutations(
          input => ItemService.addItem(input.description),
          id => ItemService.deleteItem(id.value.toString),
        ),
      )
    ) @@
      maxFields(200) @@ // query analyzer that limit query fields
      maxDepth(30) @@ // query analyzer that limit query depth
      timeout(3 seconds) @@ // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      printErrors @@ // wrapper that logs errors
      apolloTracing // wrapper for https://github.com/apollographql/apollo-tracing
}
