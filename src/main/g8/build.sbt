val zioVersion = "1.0.11"
val zioHttpVersion = "1.0.0.0-RC17"
val zioJsonVersion = "0.2.0-M1"

val zioConfigVersion = "1.0.6"
val zioLoggingVersion = "0.5.11"
val zioKafkaVersion = "0.15.0"

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        name := "zio-quickstart",
        organization := "com.example",
        version := "0.0.1",
        scalaVersion := "3.0.1",
      )
    ),
    // TODO remove, temporary solution to find zhttp-test
    // https://github.com/dream11/zio-http/issues/321
    resolvers += "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    name := "zio-quickstart",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "io.d11" %% "zhttp" % zioHttpVersion,
      "io.d11" %% "zhttp-test" % "1.0.0.0-RC17+37-1c8ceea7-SNAPSHOT" % Test,
      // TODO add below based on add_zio_kafka=yes condition in default.properties
      // "dev.zio" %% "zio-kafka"         % zioKafkaVersion,
      // TODO add here once new realease compatible with scala 3 is pushed
      // https://github.com/zio/zio-logging/pull/306
      // https://github.com/zio/zio-config/pull/599
      // "dev.zio" %% "zio-config" % zioConfigVersion,
      // "dev.zio" %% "zio-logging" % zioLoggingVersion,
      // "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-junit" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(JavaAppPackaging)
