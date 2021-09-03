package $package$.config

import zio._
import zio.config._
import zio.config.ConfigSource._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfigSource
import com.typesafe.config.ConfigFactory
import $package$.domain.DomainError
import $package$.config.configuration.ServerConfig

object configuration:

  final case class ServerConfig(port: Int, diagnosticsPort: Int)

  object ServerConfig:

    private val serverConfigDescription = (nested("server-config") {
      int("port").default(8090)
    } |@| nested("server-config")(int("diagnostics-port").default(8091)))(
      ServerConfig.apply,
      { case ServerConfig(port, diagnosticsPort) => Some((port, diagnosticsPort)) },
    )

    val layer = IO
      .fromEither(TypesafeConfigSource.fromTypesafeConfig(ConfigFactory.defaultApplication()))
      .map(source => serverConfigDescription from source)
      .flatMap(config => ZIO.fromEither(read(config)))
      .mapError(e => DomainError.ConfigError(e))
      .toLayer
