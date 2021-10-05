package $package$.repo.postgresql

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import zio._
import zio.duration._
import zio.blocking.{ effectBlocking, Blocking }

object PostgresContainer:

  def make(
      imageName: String = "postgres:alpine"
    ) =
    ZManaged.make {
      effectBlocking {
        val c = new PostgreSQLContainer(
          dockerImageNameOverride = Option(imageName).map(DockerImageName.parse)
        ).configure { a =>
          a.withInitScript("item_schema.sql")
          ()
        }
        c.start()
        c
      }
    } { container =>
      effectBlocking(container.stop()).orDie
    }.toLayer
