val dependencies = Seq(
  "org.scalanlp" %% "breeze" % "1.0",
  "org.scalanlp" %% "breeze-natives" % "1.0",
  "net.debasishg" %% "redisclient" % "3.40",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.13.0",
  "org.scalatest" %% "scalatest" % "3.2.10" % "test",
  "com.orange.redis-embedded" % "embedded-redis" % "0.6" % "test"
)

lazy val root = (project in file(".")).settings(
  name := "lsh-scala",
  organization := "io.krom",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.7",
  crossScalaVersions := Seq("2.11.12", "2.12.7"),
  libraryDependencies ++= dependencies,
  parallelExecution in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  sonatypeProfileName := "io.krom",
  pomIncludeRepository := { _ => false },
  pomExtra := (<url>https://github.com/barneygovan/lsh-scala</url>
        <licenses>
            <license>
                <name>Apache 2.0 License</name>
                <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
                <distribution>repo</distribution>
            </license>
        </licenses>
        <scm>
            <connection>scm:git:github.com/barneygovan/lsh-scala.git</connection>
            <developerConnection>scm:git:git@github.com:barneygovan/lsh-scala.git</developerConnection>
            <url>github.com/barneygovan/lsh-scala.git</url>
        </scm>
        <developers>
            <developer>
                <id>barneygovan</id>
                <name>Barney Govan</name>
                <url>https://github.com/barneygovan</url>
            </developer>
        </developers>)
)
