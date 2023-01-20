name := "viber-bot-movie-recommender"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.http4s"      %% "http4s-blaze-server" % Version.Http4sVersion,
  "org.http4s"      %% "http4s-blaze-client" % Version.Http4sVersion,
  "org.http4s"      %% "http4s-circe"        % Version.Http4sVersion,
  "org.http4s"      %% "http4s-dsl"          % Version.Http4sVersion,
  "io.circe" %% "circe-core" % Version.CirceVersion,
  "io.circe"        %% "circe-generic"       % Version.CirceVersion,
  "co.fs2" %% "fs2-io" % Version.fs2,
  "co.fs2" %% "fs2-core" % Version.fs2,
  "org.scalatest" %% "scalatest" % Version.scalatest,
  "org.scalatestplus" %% "scalacheck-1-14" % Version.scalatestplus,
  "org.slf4j" % "slf4j-api" % Version.slf4j,
  "org.slf4j" % "slf4j-simple" % Version.slf4j,
  "org.scalameta" %% "munit" % Version.Munit % Test,
  "org.typelevel" %% "munit-cats-effect-2" % Version.MunitCatsEffect % Test,
)

testFrameworks += new TestFramework("munit.Framework")
