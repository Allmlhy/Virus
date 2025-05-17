ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"

lazy val root = (project in file("."))
  .settings(
    name := "KafkaCSVToMySQL",
    libraryDependencies ++= Seq(
      // Spark 相关依赖
      "org.apache.spark" %% "spark-core" % "3.3.2",
      "org.apache.spark" %% "spark-streaming" % "3.3.2",
      "org.apache.spark" %% "spark-sql" % "3.3.2",

      // Kafka 相关依赖
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.3.2",  // 注意这里的版本要和Spark版本匹配

      // MySQL 连接器
      "mysql" % "mysql-connector-java" % "8.0.33",

      // Apache Commons CSV
      "org.apache.commons" % "commons-csv" % "1.9.0"

    ),
    resolvers ++= Seq(
      "Apache Snapshots" at "https://repository.apache.org/snapshots/"
    )
  )
