package $package$.config

import com.typesafe.config.ConfigFactory
import zio._
import zio.config._
import zio.config.typesafe._
import zio.Config._
import zio.config.typesafe.TypesafeConfigProvider

object Configuration:

  final case class ApiConfig(host: String, port: Int)

  object ApiConfig:

    private val serverConfigDescription: Config[ApiConfig] =
      (string("host") zip int("port"))
        .nested("api")
        .to[ApiConfig]

    val layer = ZLayer(
      read(
        serverConfigDescription.from(
          TypesafeConfigProvider.fromTypesafeConfig(
            ConfigFactory.defaultApplication()
          )
        )
      )
    )
