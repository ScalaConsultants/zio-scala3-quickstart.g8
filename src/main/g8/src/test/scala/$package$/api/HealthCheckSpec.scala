package $package$.api

import zio.test._
import zio.test.Assertion._
import zhttp.http._
import $package$.util._
import zio.test.assertM
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zhttp.test._
import zhttp.http.HttpData.Empty

object HealthCheckSpec extends DefaultRunnableSpec:

  def spec =
    suite("health check")(
      testM("ok status"){
        val actual = HealthCheck.healthCheck(Request(Method.GET -> URL(Root / "health")))
        assertM(actual)(equalTo(Response.HttpResponse(Status.OK, List(), Empty)))
      }
    )