val zioVersion                 = "2.0.2"
val zioHttpVersion             = "2.0.0-RC10"
val zioJsonVersion             = "0.3.0-RC10"
val logbackVersion             = "1.4.4"
$if(add_http_endpoint.truthy)$
val testcontainersVersion      = "1.17.4"
val testcontainersScalaVersion = "0.40.11"
val quillVersion               = "4.6.0"
val postgresqlVersion          = "42.5.0"
$endif$
val zioConfigVersion           = "3.0.2"
val zioMockVersion             = "1.0.0-RC8"

lazy val root = (project in file("."))
  .settings(
    name           := "$name$",
    inThisBuild(
      List(
        organization := "$package$",
        version      := "0.0.1",
        scalaVersion := "$scala_version$",
      )
    ),
    name           := "zio-quickstart",
    libraryDependencies ++= Seq(
      $if(add_http_endpoint.truthy)$
      "io.getquill"       %% "quill-jdbc-zio"                  % quillVersion excludeAll (
        ExclusionRule(organization = "org.scala-lang.modules")
      ),
      "org.postgresql"     % "postgresql"                      % postgresqlVersion,
      $endif$
      "dev.zio"           %% "zio"                             % zioVersion,
      "dev.zio"           %% "zio-streams"                     % zioVersion,
      "io.d11"            %% "zhttp"                           % zioHttpVersion,
      "dev.zio"           %% "zio-config"                      % zioConfigVersion,
      "dev.zio"           %% "zio-config-typesafe"             % zioConfigVersion,
      "ch.qos.logback"     % "logback-classic"                 % logbackVersion,
      "dev.zio"           %% "zio-json"                        % zioJsonVersion,
      "dev.zio"           %% "zio-test"                        % zioVersion                 % Test,
      "dev.zio"           %% "zio-test-sbt"                    % zioVersion                 % Test,
      "dev.zio"           %% "zio-test-junit"                  % zioVersion                 % Test,
      "dev.zio"           %% "zio-mock"                        % zioMockVersion             % Test,
      $if(add_http_endpoint.truthy)$
      "com.dimafeng"      %% "testcontainers-scala-postgresql" % testcontainersScalaVersion % Test,
      "org.testcontainers" % "testcontainers"                  % testcontainersVersion      % Test,
      "org.testcontainers" % "database-commons"                % testcontainersVersion      % Test,
      "org.testcontainers" % "postgresql"                      % testcontainersVersion      % Test,
      "org.testcontainers" % "jdbc"                            % testcontainersVersion      % Test,
      $endif$
      "dev.zio"           %% "zio-test-magnolia"               % zioVersion                 % Test,
    ),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
  )
  .enablePlugins(JavaAppPackaging)
