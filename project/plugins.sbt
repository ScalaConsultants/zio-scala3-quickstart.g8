addSbtPlugin("org.foundweekends.giter8" %% "sbt-giter8" % "0.14.0")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.6")
libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.9")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat" % "0.3.3")
