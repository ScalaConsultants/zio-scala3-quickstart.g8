package $package$.api.metricsdiagnostics

import zio.test._
import zio.test.Assertion._
import zhttp.http._
import zio.test.assertM
import zhttp.service._
import zhttp.service.server.ServerChannelFactory
import zio._
import zhttp.test._
import zhttp.http.HttpData.Empty
import zio.zmx.prometheus.PrometheusClient
import $package$.util._

object MetricsAndDiagnosticsSpec extends DefaultRunnableSpec:

  def spec =
    suite("health check")(
      testM("ok status") {
        val actual = MetricsAndDiagnostics.exposeEndpoints(Request(Method.GET -> URL(Root / "health")))
        assertM(actual)(equalTo(Response.HttpResponse(Status.OK, List(), Empty)))
      }
    ).provideCustomLayer(PrometheusClient.live)
