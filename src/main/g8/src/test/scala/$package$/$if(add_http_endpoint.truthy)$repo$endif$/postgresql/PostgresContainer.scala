package $package$.repo.postgresql

import com.dimafeng.testcontainers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import zio._

object PostgresContainer:

  def make(
      imageName: String = "postgres:alpine"
    ) =
    ZIO.acquireRelease {
      ZIO.attempt {
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
      ZIO.attempt(container.stop()).orDie
    }
