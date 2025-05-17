import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer

import java.sql.{Connection, DriverManager}
import java.text.SimpleDateFormat
import scala.collection.mutable

object VaccineStatsInserter {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("VaccineStatsInserter").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(2))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "vaccine-inserter-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("world-epidemic")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    // -------------------
    // 1. 预先加载 date_dim 到内存 Map
    val dateIdMap: Map[String, Int] = {
      var conn: Connection = null
      val map = mutable.Map[String, Int]()
      try {
        conn = DriverManager.getConnection(
          "jdbc:mysql://106.12.170.52:13327/sparkprogram?characterEncoding=utf8&useSSL=false",
          "lmx", "lmx"
        )
        val stmt = conn.createStatement()
        val rs = stmt.executeQuery("SELECT year, month, day, date_id FROM date_dim")
        while (rs.next()) {
          val key = s"${rs.getInt("year")}-${rs.getInt("month")}-${rs.getInt("day")}"
          val dateId = rs.getInt("date_id")
          map.put(key, dateId)
        }
        rs.close()
        stmt.close()
      } catch {
        case e: Exception =>
          println(s"加载 date_dim 失败: ${e.getMessage}")
          throw e
      } finally {
        if (conn != null) conn.close()
      }
      map.toMap
    }
    println(s"成功加载 date_dim，共计 ${dateIdMap.size} 条记录")

    // 日期解析函数，支持多种格式
    def parseDateFlexible(dateStr: String): Option[(Int, Int, Int)] = {
      val formats = Array("yyyy-MM-dd", "yyyy/M/d", "yyyy/MM/dd", "yyyy/M/dd", "yyyy/MM/d")
      formats.toStream.map(fmt => {
        try {
          val sdf = new SimpleDateFormat(fmt)
          val date = sdf.parse(dateStr)
          val year = date.getYear + 1900
          val month = date.getMonth + 1
          val day = date.getDate
          Some((year, month, day))
        } catch {
          case _: Exception => None
        }
      }).collectFirst { case Some(value) => value }
    }

    // -------------------

    stream.map(_.value())
      .filter(line => {
        val result = line.nonEmpty && !line.startsWith("报告日期")
        if (!result) println(s"过滤掉的行: $line")
        result
      })
      .map(line => {
        val fields = line.split(",")
        println(s"拆分字段数: ${fields.length}, 内容: ${fields.mkString("[", ", ", "]")}")
        fields
      })
      .filter(fields => {
        val result = fields.length >= 8
        if (!result) println(s"字段数量不足，跳过: ${fields.mkString(",")}")
        result
      })
      .foreachRDD { rdd =>
        println(s"本批次RDD大小: ${rdd.count()}")

        // 添加 repartition(4)
        rdd.repartition(4).foreachPartition { partition =>
          val conn = DriverManager.getConnection(
            "jdbc:mysql://106.12.170.52:13327/sparkprogram?characterEncoding=utf8&useSSL=false",
            "lmx", "lmx"
          )
          conn.setAutoCommit(false)
          val pstmt = conn.prepareStatement(
            """
              |INSERT INTO global_historical_stats (country_code, date_id, total_confirmed, total_deaths)
              |VALUES (?, ?, ?, ?)
              |ON DUPLICATE KEY UPDATE
              |  total_confirmed = VALUES(total_confirmed),
              |  total_deaths = VALUES(total_deaths)
              |""".stripMargin)

          val batchSize = 1000
          var batchCount = 0

          partition.foreach { fields =>
            try {
              val dateStr = fields(0).trim
              val countryCode = fields(1).trim
              val confirmed = fields(5).trim.toInt
              val deaths = fields(7).trim.toInt

              println(s"处理数据 - 日期: $dateStr, 国家代码: $countryCode, 确诊: $confirmed, 死亡: $deaths")

              parseDateFlexible(dateStr) match {
                case Some((year, month, day)) =>
                  val key = s"$year-$month-$day"
                  dateIdMap.get(key) match {
                    case Some(dateId) =>
                      println(s"找到对应的date_id: $dateId")
                      pstmt.setString(1, countryCode)
                      pstmt.setInt(2, dateId)
                      pstmt.setInt(3, confirmed)
                      pstmt.setInt(4, deaths)
                      pstmt.addBatch()
                      batchCount += 1

                      if (batchCount % batchSize == 0) {
                        println(s"执行批量插入，条数: $batchCount")
                        pstmt.executeBatch()
                        conn.commit()
                        pstmt.clearBatch()
                        batchCount = 0
                      }
                    case None =>
                      println(s"⚠ 未找到日期 $dateStr 对应的 date_id，跳过该条记录")
                  }
                case None =>
                  println(s"⚠ 无法识别日期格式: $dateStr，跳过该条记录")
              }
            } catch {
              case e: Exception =>
                val countryCode = if (fields.length > 1) fields(1).trim else "未知"
                println(s"⚠ 数据处理异常: ${e.getMessage}，country_code = $countryCode，字段内容：${fields.mkString("[", ", ", "]")}")
            }
          }

          if (batchCount > 0) {
            println(s"执行剩余批量插入，条数: $batchCount")
            pstmt.executeBatch()
            conn.commit()
          } else {
            println("本分区无有效数据，无需执行批量插入。")
          }

          pstmt.close()
          conn.close()
        }
      }


    ssc.start()
    ssc.awaitTermination()
  }
}
