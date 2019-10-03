name := "sgit"

version := "0.1"

scalaVersion := "2.13.1"

//Parses the command line arguments and options
libraryDependencies ++= Seq(
  "org.backuity.clist" %% "clist-core"   % "3.5.1",
  "org.backuity.clist" %% "clist-macros" % "3.5.1" % "provided"
)

//Test frameworks
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.8"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"