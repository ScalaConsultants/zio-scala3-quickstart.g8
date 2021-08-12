val zioVersion = "1.0.10"
val zioHttpVersion = "1.0.0.0-RC17"
val zioJsonVersion = "0.2.0-M1"
//val zioConfigVersion = "1.0.6"
//val zioLoggingVersion = "0.5.11"
// val zioKafkaVersion = "0.15.0"

// This build is for this Giter8 template.
// To test the template run `g8` or `g8Test` from the sbt session.
// See http://www.foundweekends.org/giter8/testing.html#Using+the+Giter8Plugin for more details.
lazy val root = (project in file("."))
  .enablePlugins(ScriptedPlugin)
  .settings(
    name := "zio-quickstart",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "io.d11" %% "zhttp" % zioHttpVersion,
//      "dev.zio" %% "zio-config" % zioConfigVersion,
//      "dev.zio" %% "zio-logging" % zioLoggingVersion,
//      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      // "dev.zio" %% "zio-kafka"         % zioKafkaVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-junit" % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
    ),
    test in Test := {
      val _ = (g8Test in Test).toTask("").value
    },
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
    scriptedLaunchOpts ++= List(
      "-Xms1024m",
      "-Xmx1024m",
      "-XX:ReservedCodeCacheSize=128m",
      "-Xss2m",
      "-Dfile.encoding=UTF-8"
    ),
    resolvers += Resolver.url(
      "typesafe",
      url("https://repo.typesafe.com/typesafe/ivy-releases/")
    )(Resolver.ivyStylePatterns)
  )
