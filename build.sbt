name := "mphb-mongodb-loader"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/org.reactivemongo/reactivemongo
  "org.reactivemongo" %% "reactivemongo" % "0.18.3",
  // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
  "org.slf4j" % "slf4j-simple" % "1.7.26" % Test
)