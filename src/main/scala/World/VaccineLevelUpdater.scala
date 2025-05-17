package World

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer
import java.sql.{Connection, DriverManager}
import java.text.SimpleDateFormat
import scala.collection.mutable

object VaccineLevelUpdater {
  def main(args: Array[String]): Unit = {
    // Spark Streaming 环境
    val conf = new SparkConf().setAppName("VaccineLevelUpdater").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(2))

    // Kafka 配置
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "vaccine-level-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )
    val topics = Array("world-epidemic")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    // 严重等级计算函数
    def computeLevel(totalConfirmed: Int): Int = {
      totalConfirmed match {
        case x if x < 100 => 0
        case x if x < 1000 => 1
        case x if x < 10000 => 2
        case x if x < 100000 => 3
        case x if x < 1000000 => 4
        case _ => 5
      }
    }

    // 处理数据流
    stream.map(_.value())
      .filter(line => line.nonEmpty && !line.startsWith("date")) // 过滤空行和表头
      .map(_.split(","))
      .filter(fields => fields.length >= 8)
      .foreachRDD { rdd =>
        rdd.repartition(4).foreachPartition { partition =>
          val conn = DriverManager.getConnection(
            "jdbc:mysql://106.12.170.52:13327/sparkprogram?characterEncoding=utf8&useSSL=false",
            "lmx", "lmx"
          )
          conn.setAutoCommit(false)

          val levelStmt = conn.prepareStatement(
            """
              |UPDATE countries
              |SET level = ?
              |WHERE country_code = ?
              |""".stripMargin)

          val batchSize = 1000
          var batchCount = 0

          // ✅ 添加排序步骤，避免交叉死锁
          partition.toList
            .sortBy(fields => fields(1)) // 按 country_code 排序
            .foreach { fields =>
              try {
                val countryCode = fields(1).trim
                val confirmed = fields(5).trim.toInt

                val level = computeLevel(confirmed)
                levelStmt.setInt(1, level)
                levelStmt.setString(2, countryCode)
                levelStmt.addBatch()

                batchCount += 1
                if (batchCount % batchSize == 0) {
                  levelStmt.executeBatch()
                  conn.commit()
                  levelStmt.clearBatch()
                  batchCount = 0
                }

              } catch {
                case e: Exception =>
                  println(s"⚠ 异常: ${e.getMessage}，数据：${fields.mkString("[", ",", "]")}")
              }
            }

          if (batchCount > 0) {
            levelStmt.executeBatch()
            conn.commit()
          }

          levelStmt.close()
          conn.close()
        }
      }

    // 启动
    ssc.start()
    ssc.awaitTermination()
  }
}
