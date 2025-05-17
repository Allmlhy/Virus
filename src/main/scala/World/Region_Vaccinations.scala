package World

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.sql.{Connection, DriverManager}
import java.text.SimpleDateFormat
import java.util.Locale
import scala.collection.mutable

object Region_Vaccinations {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("VaccineStatsInserter").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "vaccine-inserter-group",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("Region_Vaccinations")

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    // 日期映射（year-month-day -> date_id）
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
          map.put(key, rs.getInt("date_id"))
        }
        rs.close()
        stmt.close()
      } finally {
        if (conn != null) conn.close()
      }
      map.toMap
    }

    val validRegions = Set("Africa", "Asia", "Europe", "European Union", "North America", "South America", "Oceania", "World")

    stream.map(_.value())
      .filter(line => line.nonEmpty && !line.startsWith("地区")) // 去掉表头
      .map(line => line.split(",", -1))
      .filter(fields => fields.length >= 16 && validRegions.contains(fields(0).trim))
      .foreachRDD { rdd =>
        println(s"处理疫苗接种数据，本批次大小: ${rdd.count()}")
        rdd.foreachPartition { partition =>
          val conn = DriverManager.getConnection(
            "jdbc:mysql://106.12.170.52:13327/sparkprogram?characterEncoding=utf8&useSSL=false",
            "lmx", "lmx"
          )
          conn.setAutoCommit(false)
          val pstmt = conn.prepareStatement(
            """
              |INSERT INTO region_vaccinations (region_code, date_id, total_doses, vaccinated, fully_vaccinated, daily_doses, daily_vaccinated)
              |VALUES (?, ?, ?, ?, ?, ?, ?)
              |ON DUPLICATE KEY UPDATE
              |  total_doses = VALUES(total_doses),
              |  vaccinated = VALUES(vaccinated),
              |  fully_vaccinated = VALUES(fully_vaccinated),
              |  daily_doses = VALUES(daily_doses),
              |  daily_vaccinated = VALUES(daily_vaccinated)
              |""".stripMargin)

          var batchCount = 0
          val batchSize = 500

          def safeToLong(s: String): Long = {
            if (s == null || s.trim.isEmpty) 0
            else try s.trim.toDouble.toLong catch { case _: Throwable => 0 }
          }

          partition.foreach { fields =>
            try {
              val region = fields(0).trim
              val regionCode = fields(1).trim
              val dateStr = fields(2).trim
              val date = sdf.parse(dateStr)
              val year = date.getYear + 1900
              val month = date.getMonth + 1
              val day = date.getDate
              val key = s"$year-$month-$day"

              dateIdMap.get(key) match {
                case Some(dateId) =>
                  val totalDoses = safeToLong(fields(3))
                  val vaccinated = safeToLong(fields(4))
                  val fullyVaccinated = safeToLong(fields(5))
                  val dailyDoses = safeToLong(fields(8))
                  val dailyVaccinated = safeToLong(fields(14))

                  pstmt.setString(1, regionCode)
                  pstmt.setInt(2, dateId)
                  pstmt.setLong(3, totalDoses)
                  pstmt.setLong(4, vaccinated)
                  pstmt.setLong(5, fullyVaccinated)
                  pstmt.setLong(6, dailyDoses)
                  pstmt.setLong(7, dailyVaccinated)
                  pstmt.addBatch()
                  batchCount += 1

                  if (batchCount % batchSize == 0) {
                    pstmt.executeBatch()
                    conn.commit()
                    pstmt.clearBatch()
                    batchCount = 0
                  }
                case None =>
                  println(s"⚠ 未找到日期 $dateStr 对应的 date_id，跳过记录")
              }
            } catch {
              case e: Exception =>
                println(s"⚠ 插入数据异常: ${e.getMessage}，字段内容：${fields.mkString("[", ",", "]")}")
            }
          }

          if (batchCount > 0) {
            pstmt.executeBatch()
            conn.commit()
          }

          pstmt.close()
          conn.close()
        }
      }

    ssc.start()
    ssc.awaitTermination()
  }
}
