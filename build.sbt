name := """doctor-strange"""

version := "latest"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  "org.apache.spark" % "spark-core_2.11" % "1.3.0",
  "org.apache.spark" % "spark-mllib_2.11" % "1.3.0",
  "com.typesafe.play" %% "anorm" % "2.4.0",
  "com.beachape" %% "enumeratum" % "1.2.2",
  "com.beachape" %% "enumeratum-play" % "1.2.2"

)
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.27"

// Core dependencies
libraryDependencies ++= Seq(
  filters
)


// Testing dependencies
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

// Other dependencies
libraryDependencies ++= Seq(
  "org.mindrot" % "jbcrypt" % "0.3m"
)


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"