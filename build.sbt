val dependencies = Seq(
  "org.scalanlp" %% "breeze" % "0.11.2",
  "org.scalanlp" %% "breeze-natives" % "0.11.2",
  "net.debasishg" %% "redisclient" % "3.0",
  "com.lambdaworks" %% "jacks" % "2.3.3",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "com.orange.redis-embedded" % "embedded-redis" % "0.6" % "test"
)

lazy val root = (project in file(".")).settings(
  name := "lsh-scala",
  version := "0.1",
  scalaVersion := "2.11.7",
  libraryDependencies ++= dependencies,
  coverageEnabled := true
)