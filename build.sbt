name := """sbt-deploy"""

organization := "com.liveduca"

version := "1.2.3"

scalaVersion := "2.10.5"

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "com.jcraft" % "jsch" % "0.1.52",
  "com.jcraft" % "jzlib" % "1.1.3",
  "joda-time" % "joda-time" % "2.7"
)
