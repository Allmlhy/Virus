import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.{SparkSession, Row}
import org.apache.spark.sql.types._

object KafkaCSVToMySQL {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("KafkaCSVToMySQL").setMaster("local[*]")
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    val kafkaParams = Map(
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "csv-consumer",
      "auto.offset.reset" -> "latest"
    )

    val topics = Array("test")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()
    import spark.implicits._

    val schema = StructType(Array(
      StructField("id", IntegerType, true),
      StructField("name", StringType, true),
      StructField("value", IntegerType, true)
    ))

    stream.map(_.value()).foreachRDD { rdd =>
      val lines = rdd.flatMap(_.split("\n"))
      val rowRDD = lines.map(_.split(",")).filter(_.length == 3).map(arr => Row(arr(0).toInt, arr(1), arr(2).toInt))
      val df = spark.createDataFrame(rowRDD, schema)

      // 简单过滤：只保留value大于0的行
      val result = df.filter($"value" > 0)

      // 写入MySQL
      result.write
        .format("jdbc")
        .option("url", "jdbc:mysql://localhost:3306/virus")
        .option("dbtable", "virus_data")
        .option("user", "root")
        .option("password", "Leen1977!!")
        .mode("append")
        .save()
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
