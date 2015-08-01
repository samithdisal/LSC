name := "LSC"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies += "org.jsoup" % "jsoup" % "1.8.2"

libraryDependencies += "commons-cli" % "commons-cli" % "1.3.1"


mainClass in assembly := Some("app.LSC")
    