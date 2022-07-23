val zioVersion = "2.0.0"
val zioHttpVersion = "2.0.0-RC10"
val zioJsonVersion = "0.3.0-RC10"
val logbackVersion = "1.2.11"
val testcontainersVersion = "1.17.3"
val testcontainersScalaVersion = "0.40.8"
val quillVersion = "4.0.0"
val postgresqlVersion = "42.4.0"
val zioConfigVersion = "3.0.1"
val zioMockVersion = "1.0.0-RC8"

// This build is for this Giter8 template.
// To test the template run `g8` or `g8Test` from the sbt session.
// See http://www.foundweekends.org/giter8/testing.html#Using+the+Giter8Plugin for more details.
lazy val root = (project in file("."))
  .enablePlugins(ScriptedPlugin)
  .settings(
    resolvers += "Sonatype OSS Snapshots s01" at "https://s01.oss.sonatype.org/content/repositories/snapshots",
    name := "zio-quickstart",
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
