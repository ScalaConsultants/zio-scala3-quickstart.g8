package $package$.config

import com.typesafe.config.ConfigFactory
import zio._
import zio.config._
import zio.config.ConfigSource._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfigSource
import $package$.config.Configuration.ServerConfig

object Configuration:

  final case class ServerConfig(port: Int)

  object ServerConfig:

    private val serverConfigDescription =
      nested("api") {
        int("port").default(8090)
      }.to[ServerConfig]

    val layer = ZLayer(
      read(
        serverConfigDescription.from(
          TypesafeConfigSource.fromTypesafeConfig(
            ZIO.attempt(ConfigFactory.defaultApplication())
          )
        )
      )
    )
