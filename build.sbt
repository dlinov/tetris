name := "tetris"

version := "0.1"

scalaVersion := "2.12.7"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.4.0"
libraryDependencies += "org.typelevel" %% "cats-macros" % "1.4.0"
libraryDependencies += "org.typelevel" %% "cats-kernel" % "1.4.0"

libraryDependencies += "io.monix" %% "monix" % "3.0.0-RC1"

