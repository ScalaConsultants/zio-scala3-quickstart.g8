package $package$.api.graphql

import caliban.GraphQL
import caliban.GraphQL.graphQL
import caliban.RootResolver
import caliban.schema.Annotations.{ GQLDeprecated, GQLDescription }
import caliban.schema.{ ArgBuilder, GenericSchema, Schema }
import caliban.wrappers.ApolloTracing.apolloTracing
import caliban.wrappers.Wrappers._
import zio.clock.Clock
import zio.console.Console
import zio.duration._
import zio._
import zio.stream._
import $package$.domain._
import $package$.service.ItemService
import $package$.service.ItemService._

import scala.language.postfixOps

object GraphqlApi extends GenericSchema[Has[ItemService]]:

  final private case class ItemArgs(description: String)
  final private case class UpdateItemArgs(itemId: String, description: String)
  final private case class ItemIdArgs(itemId: String)

  final private case class Queries(
      @GQLDescription("Get all items")
      getAllItems: ZIO[Has[ItemService], DomainError, List[Item]],
      @GQLDescription("Get item by ID")
      getItem: ItemIdArgs => ZIO[Has[ItemService], DomainError, Option[Item]],
    )
  final private case class Mutations(
      @GQLDescription("Add new item")
      addItem: ItemArgs => ZIO[Has[ItemService], DomainError, ItemId],
      @GQLDescription("Delete item")
      deleteItem: ItemIdArgs => ZIO[Has[ItemService], DomainError, Unit],
      @GQLDescription("Update existing item")
      updateItem: UpdateItemArgs => ZIO[Has[ItemService], DomainError, Unit],
    )

  $if(add_websocket_endpoint.truthy)$
  final private case class Subscriptions(
      @GQLDescription("Stream of deleted items")
      deletedEventsStream: ZStream[Has[ItemService], Nothing, ItemId]
    )$endif$

  private val queryResolver = Queries(
    ItemService.getAllItems(),
    args => ItemService.getItemById(args.itemId),
  )

  private val mutationResolver = Mutations(
    input => ItemService.addItem(input.description),
    args => ItemService.deleteItem(args.itemId),
    update => ItemService.updateItem(update.itemId, update.description),
  )
  $if(add_websocket_endpoint.truthy)$
  private val subscriptionResolver = Subscriptions(ItemService.deletedEvents())
  $endif$
  val api: GraphQL[Console with Clock with Has[ItemService]] =
    graphQL(
      RootResolver(
        queryResolver,
        mutationResolver,
        $if(add_websocket_endpoint.truthy)$subscriptionResolver,$endif$
      )
    ) @@
      maxFields(200) @@ // query analyzer that limit query fields
      maxDepth(30) @@ // query analyzer that limit query depth
      timeout(3 seconds) @@ // wrapper that fails slow queries
      printSlowQueries(500 millis) @@ // wrapper that logs slow queries
      printErrors @@ // wrapper that logs errors
      apolloTracing // wrapper for https://github.com/apollographql/apollo-tracing
