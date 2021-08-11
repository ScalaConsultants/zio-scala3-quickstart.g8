val zioVersion = "1.0.10"
val zioHttpVersion = "1.0.0.0-RC17"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(
      List(
        name := "$name$",
        organization := "$organization$",
        version := "0.0.1",
        scalaVersion := "$dotty_version$"
      )
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"               % zioVersion,
      "dev.zio" %% "zio-test"          % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt"      % zioVersion % Test,
      "dev.zio" %% "zio-test-junit"    % zioVersion % Test,
      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
      "io.d11"  %% "d11"               $ zioHttpVersion
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
