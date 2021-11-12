val zioVersion = "1.0.12"
val zioHttpVersion = "1.0.0.0-RC17"
val zioJsonVersion = "0.2.0-M2"
val zioZMXVersion = "0.0.11"
val zioLoggingVersion = "0.5.13"
val logbackVersion = "1.2.6"
val testcontainersVersion = "1.16.2"
val testcontainersScalaVersion = "0.39.11"
val quillVersion = "3.7.2.Beta1.4"
val zioConfigVersion = "1.0.10"
val calibanVersion = "1.2.3"

// This build is for this Giter8 template.
// To test the template run `g8` or `g8Test` from the sbt session.
// See http://www.foundweekends.org/giter8/testing.html#Using+the+Giter8Plugin for more details.
lazy val root = (project in file("."))
  .enablePlugins(ScriptedPlugin)
  .settings(
    resolvers += "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    name := "zio-quickstart",
    libraryDependencies ++= Seq(
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
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "io.d11" %% "zhttp" % zioHttpVersion,
      "io.d11" %% "zhttp-test" % "1.0.0.0-RC17+37-1c8ceea7-SNAPSHOT" % Test,
      "dev.zio" %% "zio-config" % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe" % zioConfigVersion,
      "dev.zio" %% "zio-logging" % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "dev.zio" %% "zio-zmx" % zioZMXVersion,
      "dev.zio" %% "zio-json" % zioJsonVersion,
      "com.github.ghostdogpr" %% "caliban" % calibanVersion,
      "com.github.ghostdogpr" %% "caliban-zio-http" % calibanVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
      "dev.zio" %% "zio-test-junit" % zioVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % Test,
      "org.testcontainers" % "testcontainers" % testcontainersVersion % Test,
      "org.testcontainers" % "database-commons" % testcontainersVersion % Test,
      "org.testcontainers" % "postgresql" % testcontainersVersion % Test,
      "org.testcontainers" % "jdbc" % testcontainersVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
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
      "-Dfile.encoding=UTF-8",
    ),
    resolvers += Resolver.url(
      "typesafe",
      url("https://repo.typesafe.com/typesafe/ivy-releases/"),
    )(Resolver.ivyStylePatterns),
  )
