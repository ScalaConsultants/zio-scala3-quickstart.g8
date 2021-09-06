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

  final case class ServerConfig(port: Int)

  object ServerConfig:

    private val serverConfigDescription = nested("server-config") {
      int("port").default(8090)
    }(
      ServerConfig.apply,
      { case ServerConfig(port) => Some((port)) },
    )

    val layer = IO
      .fromEither(TypesafeConfigSource.fromTypesafeConfig(ConfigFactory.defaultApplication()))
      .map(source => serverConfigDescription from source)
      .flatMap(config => ZIO.fromEither(read(config)))
      .mapError(e => DomainError.ConfigError(e))
      .toLayer

  final case class DiagnosticsServerConfig(host: String, diagnosticsPort: Int, debug: Boolean)

  object DiagnosticsServerConfig:

    private val diagnosticsConfigDescription = 
      (nested("diagnostics-server-config"){
        string("diagnostics-host").default("localhost")
      } |@| nested("diagnostics-server-config"){
        int("diagnostics-port").default(8091)
      }
        |@| nested("diagnostics-server-config"){
        boolean("debug").default(false)
      })(DiagnosticsServerConfig.apply, { case DiagnosticsServerConfig(host, diagnosticsPort, debug) => Some((host, diagnosticsPort, debug))})

    val layer = IO
      .fromEither(TypesafeConfigSource.fromTypesafeConfig(ConfigFactory.defaultApplication()))
      .map(source => diagnosticsConfigDescription from source)
      .flatMap(config => ZIO.fromEither(read(config)))
      .mapError(e => DomainError.ConfigError(e))
      .toLayer