import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types._

object CombinedKafkaConsumer {
  def start(): Unit = {
    val conf = new SparkConf().setAppName("CombinedKafkaConsumer").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "combined-consumer-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("test", "test2")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._

    val schema1 = StructType(Array(
      StructField("id1", IntegerType, nullable = true),
      StructField("name1", StringType, nullable = true),
      StructField("value1", IntegerType, nullable = true)
    ))

    val schema2 = StructType(Array(
      StructField("id2", IntegerType, nullable = true),
      StructField("name2", StringType, nullable = true),
      StructField("value2", IntegerType, nullable = true)
    ))

    stream.foreachRDD { rdd =>
      val topicGrouped = rdd.map(record => (record.topic(), record.value()))
        .groupBy(_._1)

      topicGrouped.collect().foreach { case (topic, iter) =>
        val lines = iter.map(_._2).flatMap(_.split("\n"))

        val rowRDD = spark.sparkContext.parallelize(lines.toSeq)
          .map(_.split(","))
          .filter(_.length == 3)
          .flatMap(arr =>
            try {
              Some(arr)
            } catch {
              case _: NumberFormatException => None
            }
          )

        if (!rowRDD.isEmpty()) {
          topic match {
            case "test" =>
              val rows = rowRDD.map(arr => Row(arr(0).toInt, arr(1), arr(2).toInt))
              val df = spark.createDataFrame(rows, schema1)
              val result = df.filter($"value1" > 0)

              result.write
                .format("jdbc")
                .option("url", "jdbc:mysql://localhost:3306/virus?useSSL=false&serverTimezone=UTC")
                .option("dbtable", "virus_data1")
                .option("user", "root")
                .option("password", "Leen1977!!")
                .mode("append")
                .save()

            case "test2" =>
              val rows = rowRDD.map(arr => Row(arr(0).toInt, arr(1), arr(2).toInt))
              val df = spark.createDataFrame(rows, schema2)
              val result = df.filter($"value2" > 0)

              result.write
                .format("jdbc")
                .option("url", "jdbc:mysql://localhost:3306/virus?useSSL=false&serverTimezone=UTC")
                .option("dbtable", "virus_data2")
                .option("user", "root")
                .option("password", "Leen1977!!")
                .mode("append")
                .save()
          }
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
