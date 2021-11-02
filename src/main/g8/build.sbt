val zioVersion = "1.0.12"
val zioHttpVersion = "1.0.0.0-RC17"
val zioJsonVersion = "0.2.0-M1"
$if(add_metrics.truthy)$
val zioZMXVersion = "0.0.11"
$endif$
val zioLoggingVersion = "0.5.13"
val logbackVersion = "1.2.6"
$if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
val testcontainersVersion      = "1.16.2"
val testcontainersScalaVersion = "0.39.11"
val quillVersion = "3.7.2.Beta1.4"
$endif$
val zioConfigVersion = "1.0.10"
$if(add_graphql.truthy)$
val calibanVersion = "1.2.1"
$endif$

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        name := "$name$",
        organization := "$package$",
        version := "0.0.1",
        scalaVersion := "$dotty_version$",
      )
    ),
    // TODO remove, temporary solution to find zhttp-test
    // https://github.com/dream11/zio-http/issues/321
    resolvers += "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    name := "zio-quickstart",
    libraryDependencies ++= Seq(
      $if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
      "io.getquill" %% "quill-jdbc" % quillVersion excludeAll (
        ExclusionRule(organization = "org.scala-lang.modules")
      ),
      "io.getquill" %% "quill-jdbc-zio" % quillVersion excludeAll (
        ExclusionRule(organization = "org.scala-lang.modules")
      ),
      "io.getquill" %% "quill-jasync-postgres" % quillVersion excludeAll (
        ExclusionRule(organization = "org.scala-lang.modules")
      ),
      "org.postgresql" % "postgresql" % "42.3.1",
      $endif$
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "io.d11" %% "zhttp" % zioHttpVersion,
      "io.d11" %% "zhttp-test" % "1.0.0.0-RC17+37-1c8ceea7-SNAPSHOT" % Test,
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-logging" % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      $if(add_metrics.truthy)$
      "dev.zio" %% "zio-zmx" % zioZMXVersion,
      $endif$
      "dev.zio" %% "zio-json" % zioJsonVersion,
      $if(add_graphql.truthy)$
      "com.github.ghostdogpr" %% "caliban" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-zio-http" % calibanVersion,
      $endif$
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-junit" % zioVersion % Test,
      $if(add_http_endpoint.truthy||add_graphql.truthy||add_websocket_endpoint.truthy)$
      "com.dimafeng"      %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % Test,
      "org.testcontainers" % "testcontainers"                  % testcontainersVersion      % Test,
      "org.testcontainers" % "database-commons"                % testcontainersVersion      % Test,
      "org.testcontainers" % "postgresql"                      % testcontainersVersion      % Test,
      "org.testcontainers" % "jdbc"                            % testcontainersVersion      % Test,
      $endif$
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(JavaAppPackaging)
