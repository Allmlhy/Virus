package World

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Region {
  def start(): Unit = {
    val conf = new SparkConf()
      .setAppName("GlobalKafkaConsumer")
      .setMaster("local[*]")
      .set("spark.streaming.stopGracefullyOnShutdown", "true")
      .set("spark.serializer", "org.apache.spark.serializer.JavaSerializer")

    val ssc = new StreamingContext(conf, Seconds(5))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "global-vaccine-consumer-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("test3")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._

    val allowedRegions = Set(
      "Africa", "Asia", "Europe", "European Union",
      "North America", "South America", "Oceania", "World"
    )

    stream.foreachRDD { rdd =>
      val lines = rdd.map(_.value())
        .flatMap(_.split("\n"))
        .filter(_.nonEmpty)
        .filter(!_.startsWith("地区")) // 过滤表头

      if (!lines.isEmpty()) {
        val df = lines.map(_.split(",", -1))
          .filter(_.length >= 2)
          .map(arr => (arr(0).trim, arr(1).trim)) // region_name, region_code
          .filter { case (regionName, _) => allowedRegions.contains(regionName) }
          .distinct()
          .map { case (regionName, regionCode) =>
            (regionCode.take(10), regionName.take(100))
          }
          .toDF("region_code", "region_name")

        // 从 MySQL 中读取已存在数据
        val existingDF = spark.read
          .format("jdbc")
          .option("url", "jdbc:mysql://106.12.170.52:13327/sparkprogram?useSSL=false&allowPublicKeyRetrieval=true")
          .option("dbtable", "global_regions")
          .option("user", "lmx")
          .option("password", "lmx")
          .load()
          .select("region_code", "region_name")

        // 过滤已有数据
        val newData = df.join(existingDF, Seq("region_code", "region_name"), "left_anti")

        // 只插入新数据
        if (!newData.isEmpty) {
          newData.write
            .format("jdbc")
            .option("url", "jdbc:mysql://106.12.170.52:13327/sparkprogram?useSSL=false&allowPublicKeyRetrieval=true")
            .option("dbtable", "global_regions")
            .option("user", "lmx")
            .option("password", "lmx")
            .mode("append")
            .save()
        }
      }
    }

    sys.addShutdownHook {
      println("正在关闭 StreamingContext 和 SparkSession...")
      ssc.stop(stopSparkContext = true, stopGracefully = true)
      spark.stop()
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
