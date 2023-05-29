// Dependencies are needed for Scala Steward to check if there are newer versions
val zioVersion            = "2.0.13"
val zioJsonVersion        = "0.5.0"
val zioConfigVersion      = "3.0.7"
val logbackClassicVersion = "1.4.7"
val postgresqlVersion     = "42.6.0"
val testContainersVersion = "0.40.16"
val zioMockVersion        = "1.0.0-RC11"
val zioHttpVersion        = "3.0.0-RC1"
val quillVersion          = "4.6.0"

// This build is for this Giter8 template.
// To test the template run `g8` or `g8Test` from the sbt session.
// See http://www.foundweekends.org/giter8/testing.html#Using+the+Giter8Plugin for more details.
lazy val root = (project in file("."))
  .enablePlugins(ScriptedPlugin)
  .settings(
    name           := "zio-scala3-quickstart",
    Test / test    := {
      val _ = (Test / g8Test).toTask("").value
    },
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
    libraryDependencies ++= Seq(
      "io.getquill"   %% "quill-jdbc-zio"                  % quillVersion,
      "org.postgresql" % "postgresql"                      % postgresqlVersion,
      "dev.zio"       %% "zio"                             % zioVersion,
      "dev.zio"       %% "zio-streams"                     % zioVersion,
      "dev.zio"       %% "zio-http"                        % zioHttpVersion,
      "dev.zio"       %% "zio-config"                      % zioConfigVersion,
      "dev.zio"       %% "zio-config-typesafe"             % zioConfigVersion,
      "ch.qos.logback" % "logback-classic"                 % logbackClassicVersion,
      "dev.zio"       %% "zio-json"                        % zioJsonVersion,
      "dev.zio"       %% "zio-test"                        % zioVersion,
      "dev.zio"       %% "zio-test-sbt"                    % zioVersion,
      "dev.zio"       %% "zio-test-junit"                  % zioVersion,
      "dev.zio"       %% "zio-mock"                        % zioMockVersion,
      "com.dimafeng"  %% "testcontainers-scala-postgresql" % testContainersVersion,
      "dev.zio"       %% "zio-test-magnolia"               % zioVersion,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
