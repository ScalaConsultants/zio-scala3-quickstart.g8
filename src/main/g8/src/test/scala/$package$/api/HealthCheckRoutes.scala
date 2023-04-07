package $package$.api

import zhttp.http.*
import zio.test.*
import zio.test.Assertion.*

import $package$.api.healthcheck.HealthCheckServiceTest

object HealthCheckRoutesSpec extends ZIOSpecDefault:

  val specs = suite("http")(
    suite("health check")(
      test("ok status") {
        val actual =
          HealthCheckRoutes.app(Request(method = Method.GET, url = URL(!! / "healthcheck")))
        assertZIO(actual)(equalTo(Response(Status.Ok, Headers.empty, HttpData.empty)))
      }
    )
  )

  override def spec = specs.provide(
    HealthCheckServiceTest.layer
  )