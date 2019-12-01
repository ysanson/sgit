name := "sgit"

version := "2.0"

scalaVersion := "2.13.1"

mainClass in (Compile, packageBin) := Some("sgit.main.Main")
scalacOptions ++= Seq("-deprecation", "-feature")

//Parses the command line arguments and options
libraryDependencies ++= Seq(
  "org.backuity.clist" %% "clist-core"   % "3.5.1",
  "org.backuity.clist" %% "clist-macros" % "3.5.1" % "provided"
)

//File library, for better file management
libraryDependencies += "com.github.pathikrit" %% "better-files" % "3.8.0"

//Test frameworks
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.8" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1" % "test"
parallelExecution in Test := false
