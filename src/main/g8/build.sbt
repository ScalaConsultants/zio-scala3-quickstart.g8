val zioVersion = "1.0.10"
val zioHttpVersion = "1.0.0.0-RC17"
val zioJsonVersion = "0.2.0-M1"
//val zioConfigVersion = "1.0.6"
//val zioLoggingVersion = "0.5.11"
//val zioKafkaVersion = "0.15.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(
      List(
        name := "$name$",
        organization := "$organization$",
        version := "0.0.1",
        scalaVersion := "$dotty_version$",
      )
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"               % zioVersion,
      "dev.zio" %% "zio-streams"       % zioVersion,
      "io.d11"  %% "zhttp"             % zioHttpVersion,
      //TODO add below based on add_zio_kafka=yes condition in default.properties
      //"dev.zio" %% "zio-kafka"         % zioKafkaVersion,
      //"dev.zio" %% "zio-config" % zioConfigVersion,
      //"dev.zio" %% "zio-logging" % zioLoggingVersion,
//      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      "dev.zio" %% "zio-json"          % zioJsonVersion,
      "dev.zio" %% "zio-test"          % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio" %% "zio-test-junit"    % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
      
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
