name := """sbt-deploy"""

organization := "com.eadive"

version := "1.2.5"

scalaVersion := "2.10.5"

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.jcraft" % "jsch" % "0.1.52",
  "com.jcraft" % "jzlib" % "1.1.3",
  "joda-time" % "joda-time" % "2.7"
)

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/carlos8080/sbt-deploy</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:carlos8080/sbt-deploy.git</url>
    <connection>scm:git:git@github.com:carlos8080/sbt-deploy.git</connection>
  </scm>
  <developers>
    <developer>
      <id>carlos8080</id>
      <name>Carlos Souza</name>
      <url>https://github.com/carlos8080</url>
    </developer>
  </developers>)