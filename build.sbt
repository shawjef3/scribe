name := "scribe"
organization in ThisBuild := "com.outr"
version in ThisBuild := "1.4.5"
scalaVersion in ThisBuild := "2.12.2"
crossScalaVersions in ThisBuild := List("2.12.2", "2.11.11")

lazy val root = project.in(file("."))
  .aggregate(js, jvm, slf4j, slack, benchmarks)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val scribe = crossProject.in(file("."))
    .settings(
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
    )

lazy val js = scribe.js
lazy val jvm = scribe.jvm

lazy val slf4j = project.in(file("slf4j"))
  .dependsOn(jvm)
  .settings(
    name := "scribe-slf4j",
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.scalatest" %% "scalatest" % "3.0.3" % "test"
    )
  )

lazy val slack = project.in(file("slack"))
  .dependsOn(jvm)
  .settings(
    name := "scribe-slack",
    libraryDependencies += "com.eed3si9n" %% "gigahorse-asynchttpclient" % "0.3.1",
    libraryDependencies += "com.lihaoyi" %% "upickle" % "0.4.4"
  )

lazy val benchmarks = project.in(file("benchmarks"))
  .dependsOn(jvm)
  .enablePlugins(JmhPlugin)
  .settings(
    publishArtifact := false,
    libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.10.0",
    libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.10.0"
  )
