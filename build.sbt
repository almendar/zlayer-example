val zio_V = "1.0.0-RC18-2"

lazy val zlayerExample =
  project
    .in(file("."))
    .settings(
      scalaVersion := "2.13.1",
      turbo in ThisBuild := true,
      testFrameworks in ThisBuild += new TestFramework("zio.test.sbt.ZTestFramework"),
      fork := true
    )
    .settings(
      libraryDependencies ++= Seq("zio", "zio-streams", "zio-test", "zio-test-sbt").map(
        artifact => "dev.zio" %% artifact % zio_V
      )
    )