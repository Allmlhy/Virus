package World

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.commons.csv._
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import java.text.SimpleDateFormat
import scala.collection.mutable
import scala.util.control.Breaks._
import java.util.concurrent.{Executors, TimeUnit}
import scala.math.log

object EpidemicStatsUpdater {
  // 连接信息
  val jdbcUrl = "jdbc:mysql://106.12.170.52:13327/sparkprogram?characterEncoding=utf8&useSSL=false"
  val jdbcUser = "lmx"
  val jdbcPassword = "lmx"

  // 预先加载 date_dim 到内存 Map：key = "yyyy-M-d", value = date_id
  val dateIdMap: Map[String, Int] = {
    var conn: Connection = null
    val map = mutable.Map[String, Int]()
    try {
      println("[DEBUG] 连接数据库，准备加载 date_dim 表...")
      conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)
      val stmt = conn.createStatement()
      val rs = stmt.executeQuery("SELECT year, month, day, date_id FROM date_dim")
      var count = 0
      while (rs.next()) {
        val key = s"${rs.getInt("year")}-${rs.getInt("month")}-${rs.getInt("day")}"
        val dateId = rs.getInt("date_id")
        map.put(key, dateId)
        count += 1
      }
      println(s"[DEBUG] date_dim 加载完成，共 $count 条记录")
      rs.close()
      stmt.close()
    } catch {
      case e: Exception =>
        println(s"[ERROR] 加载 date_dim 失败: ${e.getMessage}")
        throw e
    } finally {
      if (conn != null) {
        conn.close()
        println("[DEBUG] 关闭数据库连接（date_dim 加载）")
      }
    }
    map.toMap
  }
  println(s"[INFO] 成功加载 date_dim，共计 ${dateIdMap.size} 条记录")

  def main(args: Array[String]): Unit = {
    println("[INFO] 启动 Spark Streaming 应用 EpidemicStatsUpdater...")
    val conf = new SparkConf().setAppName("EpidemicStatsUpdater").setMaster("local[*]")
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

    def parseDateFlexible(dateStr: String): Option[(Int, Int, Int)] = {
      val formats = Array("yyyy-MM-dd", "yyyy/M/d", "yyyy/MM/dd", "yyyy/M/dd", "yyyy/MM/d", "yyyy/M/d")
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

    def previousDayKey(year: Int, month: Int, day: Int): String = {
      val cal = new java.util.GregorianCalendar(year, month - 1, day)
      cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
      val prevYear = cal.get(java.util.Calendar.YEAR)
      val prevMonth = cal.get(java.util.Calendar.MONTH) + 1
      val prevDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
      s"$prevYear-$prevMonth-$prevDay"
    }

    stream.map(_.value())
      .filter(line => {
        val cond = line.nonEmpty && !line.startsWith("报告日期")
        if (!cond) println(s"[DEBUG] 过滤掉无效或表头行: $line")
        cond
      })
      .map(line => {
        println(s"[DEBUG] 处理数据行: $line")
        val parser = CSVParser.parse(line, CSVFormat.DEFAULT)
        val record = parser.getRecords.get(0)
        val fields = (0 until record.size()).map(i => record.get(i)).toArray
        fields
      })
      .filter(fields => {
        val cond = fields.length >= 8
        if (!cond) println(s"[WARN] 数据字段不足8个，丢弃: ${fields.mkString(",")}")
        cond
      })
      .foreachRDD { rdd =>
        println(s"[INFO] RDD 分区数: ${rdd.getNumPartitions}, 记录数: ${rdd.count()}")
        rdd.repartition(4).foreachPartition { partition =>
          println(s"[DEBUG] 新建数据库连接，处理一个分区数据...")
          val conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)
          conn.setAutoCommit(false)

          val insertSql =
            """
              |INSERT INTO global_daily_stats (country_code, date_id, new_deaths,new_confirmed,  v, a)
              |VALUES (?, ?, ?, ?, ?, 0)
              |ON DUPLICATE KEY UPDATE
              |  new_confirmed = VALUES(new_confirmed),
              |  new_deaths = VALUES(new_deaths),
              |  v = VALUES(v)
          """.stripMargin

          val pstmt = conn.prepareStatement(insertSql)

          val batchSize = 1000
          var batchCount = 0
          var processedCount = 0

          partition.foreach { fields =>
            breakable {
              try {
                val dateStr = fields(0).trim
                val countryCode = fields(1).trim
                val newConfirmed = fields(4).trim.toInt
                val newDeaths = fields(6).trim.toInt

                parseDateFlexible(dateStr) match {
                  case Some((year, month, day)) =>
                    val dateKey = s"$year-$month-$day"
                    dateIdMap.get(dateKey) match {
                      case Some(dateId) =>
                        val prevKey = previousDayKey(year, month, day)
                        val prevDateIdOpt = dateIdMap.get(prevKey)
                        val prevNewConfirmed = prevDateIdOpt.map(prevId => {
                          val ps = conn.prepareStatement(
                            "SELECT new_confirmed FROM global_daily_stats WHERE country_code = ? AND date_id = ?"
                          )
                          ps.setString(1, countryCode)
                          ps.setInt(2, prevId)
                          val rs = ps.executeQuery()
                          val res = if (rs.next()) rs.getInt("new_confirmed") else 0
                          rs.close()
                          ps.close()
                          res
                        }).getOrElse(0)

                        val v: Double = log(newConfirmed + 1) - log(prevNewConfirmed + 1)

                        println(s"[DEBUG] countryCode=$countryCode, date=$dateKey, newConfirmed=$newConfirmed, prevNewConfirmed=$prevNewConfirmed, v=$v")

                        pstmt.setString(1, countryCode)
                        pstmt.setInt(2, dateId)
                        pstmt.setInt(3, newDeaths)
                        pstmt.setInt(4, newConfirmed)

                        pstmt.setDouble(5, v)
                        pstmt.addBatch()
                        batchCount += 1
                        processedCount += 1

                        if (batchCount % batchSize == 0) {
                          println(s"[DEBUG] 执行批量插入，批量大小: $batchCount")
                          pstmt.executeBatch()
                          conn.commit()
                          pstmt.clearBatch()
                          batchCount = 0
                        }
                      case None =>
                        println(s"[WARN] 找不到 dateId 对应的日期键: $dateKey，跳过此条记录")
                        break
                    }
                  case None =>
                    println(s"[WARN] 日期解析失败: $dateStr，跳过此条记录")
                    break
                }
              } catch {
                case e: Exception =>
                  println(s"[ERROR] 处理记录异常: ${e.getMessage}")
                  break
              }
            }
          }

          if (batchCount > 0) {
            println(s"[DEBUG] 执行最后一批插入，批量大小: $batchCount")
            pstmt.executeBatch()
            conn.commit()
          }

          println(s"[INFO] 分区处理完成，共处理 $processedCount 条记录")
          pstmt.close()
          conn.close()
          println("[DEBUG] 关闭数据库连接（分区处理结束）")
        }
      }

    ssc.start()
    ssc.awaitTermination()
  }
}
