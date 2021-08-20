package $package$.api

import zio.*
import zhttp.http.*
import zhttp.socket.*
import zio.stream.*
import zio.console.*
import $package$.service.BusinessLogicService.*
import $package$.service.BusinessLogicService

object WebSocketRoute:
  private val open = Socket.succeed(WebSocketFrame.text("ws openned!"))
  private val decoder = SocketDecoder.allowExtensions
  private val socket = Socket.collect[WebSocketFrame] {
    case WebSocketFrame.Text("deleted") =>
      deletedEvents().map { itemId =>
        WebSocketFrame.text(s"deleted: \${itemId.value}")
      }
    case fr @ WebSocketFrame.Text(_) =>
      ZStream
        .fromEffect {
          getItemById(fr.text).map(
            _.fold(WebSocketFrame.text("item doesn't exist"))(item =>
              WebSocketFrame.text(item.toString)
            )
          )
        }
  }
  private val socketApp =
    SocketApp.open(open)
      ++ SocketApp.message(socket)
      ++ SocketApp.close(_ => console.putStrLn("Closed!").ignore)
      ++ SocketApp.error(_ => console.putStrLn("Error!").ignore)
      ++ SocketApp.decoder(decoder)

  val socketImpl: HttpApp[Has[BusinessLogicService] with Console, Throwable] = HttpApp.collectM {
    case Method.GET -> Root / "ws" / "items" => Task.effect(socketApp)
  }