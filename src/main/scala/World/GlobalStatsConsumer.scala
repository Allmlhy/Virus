package World

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.time.format.DateTimeFormatter

object GlobalStatsConsumer {
  // 使用线程安全的 DateTimeFormatter
  private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def safeToInt(s: String): Int = try { s.toInt } catch { case _: Exception => 0 }
  def safeToFloat(s: String): Float = try { s.toFloat } catch { case _: Exception => 0f }

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
      "group.id" -> "global-consumer-group",
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

    stream.foreachRDD { rdd =>
      val lines = rdd.map(_.value())
        .flatMap(_.split("\n"))
        .filter(_.nonEmpty)
        .filter(!_.startsWith("报告日期"))

      if (!lines.isEmpty()) {
        val df = lines.map(_.split(",", -1))
          .filter(_.length >= 8)
          .map(arr => (
            arr(0), arr(1), arr(2), arr(3),
            safeToInt(arr(4)), safeToInt(arr(5)), safeToInt(arr(6)), safeToInt(arr(7))
          )).toDF("report_date", "country_code", "country", "who_region",
            "new_confirmed", "total_confirmed", "new_deaths", "total_deaths")

        df.cache()

//        // 2. countries
//        df.select("country_code", "country", "who_region", "total_confirmed", "total_deaths").distinct()
//          .map { row =>
//            val code = row.getString(0).take(10)
//            val name = row.getString(1).take(100)
//            val region = row.getString(2).take(10)
//            val confirmed = row.getInt(3)
//            val deaths = row.getInt(4)
//            val level = math.min(math.max(if (confirmed == 0) 0 else ((deaths.toDouble / confirmed) * 5).toInt + 1, 0), 5)
//            (code, name, region, level)
//          }
//          .toDF("country_code", "country_name", "region_code", "level")
//          .write
//          .format("jdbc")
//          .option("url", "jdbc:mysql://106.12.170.52:13327/sparkprogram?useSSL=false&allowPublicKeyRetrieval=true")
//          .option("dbtable", "countries")
//          .option("user", "lmx")
//          .option("password", "lmx")
//          .mode("append")
//          .save()



        // 4. global_historical_stats
//        df.map(row => (
//            row.getString(1),
//            try { row.getString(0).replace("-", "").toInt } catch { case _: Exception => 0 },
//            row.getInt(5),
//            row.getInt(7)
//          )).filter(_._2 != 0)
//          .toDF("country_code", "date_id", "total_confirmed", "total_deaths")
//          .write
//          .format("jdbc")
//          .option("url", "jdbc:mysql://106.12.170.52:13327/sparkprogram?useSSL=false&allowPublicKeyRetrieval=true")
//          .option("dbtable", "global_historical_stats")
//          .option("user", "lmx")
//          .option("password", "lmx")
//          .mode("append")
//          .save()

        // 5. global_daily_stats
//        val daily = df.select("report_date", "country_code", "new_confirmed", "new_deaths")
//          .map(row => (
//            row.getString(1),
//            row.getString(0),
//            row.getInt(2),
//            row.getInt(3)
//          ))
//          .rdd.groupBy(_._1)
//          .flatMap { case (country, records) =>
//            val sorted = records.toList.sortBy(_._2)
//            sorted.zipWithIndex.map { case ((_, dateStr, newC, newD), idx) =>
//              try {
//                val prevC = if (idx > 0) sorted(idx - 1)._3 else 0
//                val prevPrevC = if (idx > 1) sorted(idx - 2)._3 else 0
//                val v = (newC - prevC).toFloat
//                val a = (v - (prevC - prevPrevC)).toFloat
//                (country, dateStr.replace("-", "").toInt, newD, newC, v, a)
//              } catch {
//                case e: Exception =>
//                  println(s"计算v/a时出错: ${e.getMessage}")
//                  (country, 0, 0, 0, 0f, 0f)
//              }
//            }
//          }.filter(_._2 != 0)
//          .toDF("country_code", "date_id", "new_deaths", "new_confirmed", "v", "a")
//
//        daily.write
//          .format("jdbc")
//          .option("url", "jdbc:mysql://106.12.170.52:13327/sparkprogram?useSSL=false&allowPublicKeyRetrieval=true")
//          .option("dbtable", "global_daily_stats")
//          .option("user", "lmx")
//          .option("password", "lmx")
//          .mode("append")
//          .save()

        df.unpersist()
      }
    }

    // 优雅关闭 StreamingContext 和 SparkSession
    sys.addShutdownHook {
      println("正在关闭 StreamingContext 和 SparkSession...")
      ssc.stop(stopSparkContext = true, stopGracefully = true)
      spark.stop()
    }

    ssc.start()
    ssc.awaitTermination()
  }
}



// 3. date_dim
//        df.select("report_date").distinct()
//          .map { row =>
//            try {
//              val date = LocalDate.parse(row.getString(0), dateFormatter)
//              (row.getString(0).replace("-", "").toInt, date.getYear, date.getMonthValue, date.getDayOfMonth)
//            } catch {
//              case e: DateTimeException =>
//                println(s"日期解析错误: ${row.getString(0)}")
//                (0, 0, 0, 0)
//            }
//          }
//          .filter(_._1 != 0)
//          .toDF("date_id", "year", "month", "day")
//          .write
//          .format("jdbc")
//          .option("url", "jdbc:mysql://106.12.170.52:13327/sparkprogram?useSSL=false&allowPublicKeyRetrieval=true")
//          .option("dbtable", "date_dim")
//          .option("user", "lmx")
//          .option("password", "lmx")
//          .mode("append")
//          .save()
