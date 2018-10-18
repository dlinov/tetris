import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val sharedSettings = Seq(
  scalaVersion := "2.12.7",
  version := "0.2",
  scalacOptions ++= Seq(
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:postfixOps",
    "-Ypartial-unification"
  ),
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % catsVersion,
    "org.typelevel" %%% "cats-macros" % catsVersion,
    "org.typelevel" %%% "cats-kernel" % catsVersion,
    "io.monix" %%% "monix" % monixVersion
  )
)

val catsVersion = "1.4.0"
val http4sVersion = "0.18.19"
val scalaTagsVersion = "0.6.7"
val logbackVersion = "1.2.3"
val monixVersion = "3.0.0-RC1"

lazy val frontend = (project in file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(shared.js)
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.6")
  )

lazy val backend = (project in file("backend"))
  .dependsOn(shared.jvm)
  .settings(sharedSettings)
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "com.lihaoyi" %%% "scalatags" % scalaTagsVersion
    ),
    // Allows to read the generated JS on client
    resources in Compile += (fastOptJS in (frontend, Compile)).value.data,
    // Lets the backend to read the .map file for js
    resources in Compile += (fastOptJS in (frontend, Compile)).value
      .map((x: sbt.File) => new File(x.getAbsolutePath + ".map"))
      .data,
    // Lets the server read the jsdeps file
    (managedResources in Compile) += (artifactPath in (frontend, Compile, packageJSDependencies)).value,
    // do a fastOptJS on reStart
    reStart := (reStart dependsOn (fastOptJS in (frontend, Compile))).evaluated,
    // This settings makes reStart to rebuild if a scala.js file changes on the client
    watchSources ++= (watchSources in frontend).value,
    // Support stopping the running server
    mainClass in reStart := Some("com.github.nikalaikina.tetris.http.Boot")
  )

  // configure Scala-Native settings
  //.nativeSettings(/* ... */) // defined in sbt-scala-native

lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(sharedSettings)
  .settings(name := "shared")
