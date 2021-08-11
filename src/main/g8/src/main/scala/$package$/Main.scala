package $package$

import java.io.IOException

import zio.{ App, ExitCode, URIO, ZEnv, ZIO }
import zio.console.{ getStrLn, putStrLn, Console }

object Main extends App:

  val app: ZIO[Console, IOException, Unit] =
    for
      _    <- putStrLn("What is your name?")
      name <- getStrLn
      out  <- putStrLn(s"Hello \$name!")
    yield out

  def run(args: List[String]): URIO[ZEnv, ExitCode] =
    app.exitCode
