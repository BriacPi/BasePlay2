name := """doctor-strange"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  evolutions,
  specs2 % Test,
  "org.apache.spark" % "spark-core_2.11" % "1.3.0",
  "org.apache.spark" % "spark-mllib_2.11" % "1.3.0",
  "com.typesafe.play" %% "anorm" % "2.3.6",
  "com.beachape" %% "enumeratum" % "1.2.2",
  "com.beachape" %% "enumeratum-play" % "1.2.2"
)
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.27"

// Core dependencies
libraryDependencies ++= Seq(
  filters
)

// Hystrix dependencies
libraryDependencies ++= Seq(
  "com.netflix.hystrix" % "hystrix-core" % "1.4.12",
  "com.netflix.rxjava"  % "rxjava-scala" % "0.20.7"
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


//resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"