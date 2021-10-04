package $package$.repo.postgresql

import zio._
import java.sql.Connection
import java.sql.SQLException
import java.sql.DriverManager
import com.dimafeng.testcontainers.PostgreSQLContainer
import zio.blocking._
import zio.clock._
import zio.duration._
import java.util.Properties

trait ConnectionBuilder:
  def connection: Managed[SQLException, Connection]

object ConnectionBuilder:
  def connection: ZManaged[Has[ConnectionBuilder], SQLException, Connection] =
    ZManaged.serviceWithManaged[ConnectionBuilder](_.connection)

final class ConnectionBuilderLive(
    container: PostgreSQLContainer,
    blocking: Blocking.Service,
    clock: Clock.Service,
  ) extends ConnectionBuilder:

  def connection: Managed[SQLException, Connection] =
    ZManaged.make(createConnection())(conn => ZIO.effect(conn.close).orDie)

  private def createConnection(): IO[SQLException, Connection] =
    blocking
      .effectBlocking(
        DriverManager.getConnection(
          container.jdbcUrl,
          connProperties(container.username, container.password),
        )
      )
      .retry(Schedule.recurs(20) && Schedule.exponential(10.millis))
      .provide(Has(clock))
      .refineOrDie { case e: SQLException => e }

  private def connProperties(user: String, password: String): Properties =
    val props = new Properties
    props.setProperty("user", user)
    props.setProperty("password", password)
    props

object ConnectionBuilderLive:
  val layer
      : ZLayer[Has[PostgreSQLContainer] with Blocking with Clock, Nothing, Has[ConnectionBuilder]] =
    (for {
      container <- ZIO.service[PostgreSQLContainer]
      blocking <- ZIO.service[Blocking.Service]
      clock <- ZIO.service[Clock.Service]
    } yield ConnectionBuilderLive(container, blocking, clock)).toLayer