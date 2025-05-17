package World

import org.apache.commons.csv.{CSVFormat, CSVParser}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.sql.{Connection, DriverManager}

object Daily_Stats_VA {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("CalculateV").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(30))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "calculate-v-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("world_daily_stats_va")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    stream.foreachRDD { rdd =>
      println(s"📦 批次数据接收完毕，共 ${rdd.count()} 条消息，开始计算 v")

      rdd.foreachPartition { partition =>
        var conn: Connection = null
        try {
          conn = DriverManager.getConnection(
            "jdbc:mysql://106.12.170.52:13327/sparkprogram?characterEncoding=utf8&useSSL=false",
            "lmx", "lmx"
          )
          conn.setAutoCommit(false)

          val updateStmt = conn.prepareStatement(
            """
              |UPDATE global_daily_stats
              |SET v = ?
              |WHERE country_code = ? AND date_id = ?
              |""".stripMargin
          )

          partition.foreach { record =>
            val message = record.value()
            println(s"📤 处理消息: $message")

            try {
              // 使用 Apache Commons CSV 解析一行 CSV 数据，支持带引号的逗号
              val parser = CSVParser.parse(message, CSVFormat.DEFAULT)
              val csvRecord = parser.getRecords.get(0)

              val dateStr = csvRecord.get(0).trim            // 日期，如 2020-01-23
              val countryCode = csvRecord.get(1).trim        // 国家代码，如 BQ
              val newConfirmed = csvRecord.get(4).trim.toInt // 新确诊数
              val dateId = dateStr.replaceAll("-", "").toInt // 转换为整数日期，如 20200123

              // 查询前一天数据
              val prevStats = getPreviousDayStats(conn, countryCode, dateId)
              if (prevStats.nonEmpty) {
                val (prevDateId, prevConfirmed) = prevStats.head

                // 计算 v
                val v = if (prevConfirmed > 0) {
                  ((newConfirmed - prevConfirmed).toFloat / prevConfirmed) * 100
                } else 0f

                // 更新数据库
                updateStmt.setFloat(1, v)
                updateStmt.setString(2, countryCode)
                updateStmt.setInt(3, dateId)
                updateStmt.addBatch()

                println(s"✅ 计算完成: country=$countryCode, date_id=$dateId, new_confirmed=$newConfirmed, v=$v")
              } else {
                println(s"⚠️ 未找到前一天数据: country=$countryCode, date_id=$dateId")
              }
            } catch {
              case e: Exception =>
                println(s"❌ 解析消息错误: $message")
                e.printStackTrace()
            }
          }

          // 执行批量更新
          updateStmt.executeBatch()
          conn.commit()
          updateStmt.close()
          println("✅ 批次处理完成，已更新所有国家的 v")

        } catch {
          case e: Exception =>
            println(s"❌ 批次处理出错: ${e.getMessage}")
            e.printStackTrace()
        } finally {
          if (conn != null) conn.close()
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }

  // 获取前一天的数据（仅返回 new_confirmed）
  def getPreviousDayStats(conn: Connection, countryCode: String, dateId: Int): List[(Int, Int)] = {
    val stmt = conn.prepareStatement(
      "SELECT date_id, new_confirmed FROM global_daily_stats WHERE country_code = ? AND date_id < ? ORDER BY date_id DESC LIMIT 1"
    )
    stmt.setString(1, countryCode)
    stmt.setInt(2, dateId)
    val rs = stmt.executeQuery()
    val buffer = scala.collection.mutable.ListBuffer[(Int, Int)]()
    while (rs.next()) {
      buffer += ((rs.getInt("date_id"), rs.getInt("new_confirmed")))
    }
    rs.close()
    stmt.close()
    buffer.toList
  }
}
