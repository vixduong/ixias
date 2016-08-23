/*
 * This file is part of the ixias service.
 *
 * For the full copyright and license information,
 * please view the LICENSE file that was distributed with this source code.
 */

lazy val commonSettings = Seq(
  organization := "net.ixias",
  scalaVersion := "2.11.8",
  resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype Release"  at "https://oss.sonatype.org/content/repositories/releases/",
    "Sonatype Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots/"
  ),
  // Scala compile options
  scalacOptions ++= Seq(
    "-deprecation",            // Emit warning and location for usages of deprecated APIs.
    "-feature",                // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked",              // Enable additional warnings where generated code depends on assumptions.
    "-Xfatal-warnings",        // Fail the compilation if there are any warnings.
    "-Xlint",                  // Enable recommended additional warnings.
    "-Ywarn-adapted-args",     // Warn if an argument list is modified to match the receiver.
    "-Ywarn-dead-code",        // Warn when dead code is identified.
    "-Ywarn-inaccessible",     // Warn about inaccessible types in method signatures.
    "-Ywarn-nullary-override", // Warn when non-nullary overrides nullary, e.g. def foo() over def foo.
    "-Ywarn-numeric-widen"     // Warn when numerics are widened.
  ),
  libraryDependencies ++= Seq(
    "org.specs2"      %% "specs2-core"          % "3.8.3"  % Test,
    "org.specs2"      %% "specs2-matcher-extra" % "3.8.3"  % Test,
    "ch.qos.logback"   % "logback-classic"      % "1.1.3"  % Test,
    "mysql"            % "mysql-connector-java" % "5.1.38" % Test
  )
)

lazy val playSettings = Seq(
  unmanagedSourceDirectories   in Compile += baseDirectory.value / "src" / "main" / "scala",
  unmanagedSourceDirectories   in Test    += baseDirectory.value / "src" / "test" / "scala",
  unmanagedResourceDirectories in Test    += baseDirectory.value / "src" / "test" / "resources",
  libraryDependencies ++= Seq(ws, cache,
    "org.abstractj.kalium" % "kalium" % "0.5.0"
  )
)


// Publisher Setting
//~~~~~~~~~~~~~~~~~~~
import ReleaseTransformations._
lazy val publisherSettings = Seq(
  publishTo := {
    val branch  = "git branch".lines_!.find{_.head == '*'}.map{_.drop(2)}.getOrElse("")
    val release = (branch == "master" || branch.startsWith("release"))
    val path = if (release) "releases" else "snapshots"
    Some("Nextbeat snapshots" at "s3://maven.ixias.net.s3-ap-northeast-1.amazonaws.com/" + path)
  },
  publishArtifact in (Compile, packageDoc) := false, // disable publishing the Doc jar
  publishArtifact in (Compile, packageSrc) := false, // disable publishing the sources jar
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    pushChanges
  )
)

// IxiaS Core Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasCore = (project in file("framework/ixias-core"))
  .settings(name := "ixias-core")
  .settings(commonSettings:    _*)
  .settings(publisherSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe"        % "config"          % "1.3.0",
    "com.typesafe.slick" %% "slick"           % "3.1.1",
    "com.zaxxer"          % "HikariCP"        % "2.4.6",
    "com.bionicspirit"   %% "shade"           % "1.7.3",
    "org.slf4j"           % "slf4j-api"       % "1.7.21",
    "joda-time"           % "joda-time"       % "2.9.4",
    "org.joda"            % "joda-convert"    % "1.8.1"
  ))

// IxiaS Play Libraries
//~~~~~~~~~~~~~~~~~~~~~~~~~~~
lazy val ixiasPlayCore = (project in file("framework/ixias-play-core"))
  .settings(name := "ixias-play-core")
  .enablePlugins(PlayScala)
  .dependsOn(ixiasCore)
  .settings(commonSettings:    _*)
  .settings(playSettings:      _*)
  .settings(publisherSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "org.scala-lang"        % "scala-compiler" % scalaVersion.value,
    "org.scalatra.scalate" %% "scalate-core"   % "1.7.1"
  ))

lazy val ixiasPlayAuth = (project in file("framework/ixias-play-auth"))
  .settings(name := "ixias-play-auth")
  .enablePlugins(PlayScala)
  .dependsOn(ixiasCore)
  .settings(commonSettings:    _*)
  .settings(playSettings:      _*)
  .settings(publisherSettings: _*)

// IxiaS Meta Packages
//~~~~~~~~~~~~~~~~~~~~~
lazy val ixias = (project in file("."))
  .settings(name := "ixias")
  .settings(commonSettings:    _*)
  .settings(publisherSettings: _*)
  .aggregate(ixiasCore, ixiasPlay)
  .dependsOn(ixiasCore, ixiasPlay)

lazy val ixiasPlay = (project in file("target/ixias-play"))
  .settings(name := "ixias-play")
  .settings(commonSettings:    _*)
  .settings(publisherSettings: _*)
  .aggregate(ixiasPlayCore, ixiasPlayAuth)
  .dependsOn(ixiasPlayCore, ixiasPlayAuth)

// Setting for prompt
import com.scalapenos.sbt.prompt._
val defaultTheme = PromptTheme(List(
  text("[SBT] ", fg(green)),
  text(state => { Project.extract(state).get(organization) + "@" }, fg(magenta)),
  text(state => { Project.extract(state).get(name) },               fg(magenta)),
  text(":", NoStyle),
  gitBranch(clean = fg(green), dirty = fg(yellow)).padLeft("[").padRight("]"),
  text(" > ", NoStyle)
))
promptTheme := defaultTheme
shellPrompt := (implicit state => promptTheme.value.render(state))
