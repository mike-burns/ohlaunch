import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "Oh! Launch!",
    version := "0.1",
    scalaVersion := "2.9.0-1",
    platformName in Android := "android-4"
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "ohlaunch",
      proguardInJars in Android ++= ((file("lib") ** "*.jar") get),
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "1.6.1" % "test",
        "org.positronicnet" %% "positronicnetlib" % "0.3-SNAPSHOT")
    )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "Oh! Launch!",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++ AndroidTest.androidSettings
  ) dependsOn main
}