import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import java.time.LocalDate

object HistoryCovidCityExtractor {

  def start(): Unit = {
    val conf = new SparkConf().setAppName("HistoryCovidCityExtractor").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "covid-city-group-1",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("china-covid-history-data-temp-temp")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    val spark = SparkSession.builder().config(conf).getOrCreate()

    stream.foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        val lines = rdd.map(record => record.value())
        val chinaData = lines
          .map(_.split(",", -1))
          .filter(fields => fields.length >= 16 && fields(0) == "中国")

        chinaData.foreachPartition { partition =>
          // JDBC连接信息
          val url = "jdbc:mysql://106.12.170.52:13327/sparkprogram?allowPublicKeyRetrieval=true&useSSL=false"
          val user = "lmx"
          val password = "lmx"
          var connection: Connection = null

          try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            connection = DriverManager.getConnection(url, user, password)

            val selectRegionStmt = connection.prepareStatement("SELECT region_id FROM regions WHERE province = ? AND city = ?")
            val selectDateStmt = connection.prepareStatement("SELECT date_id FROM date_dim WHERE year = ? AND month = ? AND day = ?")

            val insertStatsStmt = connection.prepareStatement(
              """INSERT INTO historical_stats (region_id, date_id, total_confirmed, total_deaths, total_recovered,
                |total_imported, total_asymptomatic, current_confirmed)
                |VALUES (?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin)

            partition.foreach { fields =>
              val province = fields(1).trim
              val city = fields(2).trim
              val totalConfirmed = fields(3).trim.toInt
              val totalDeaths = fields(6).trim.toInt
              val totalRecovered = fields(9).trim.toInt
              val totalImported = fields(13).trim.toInt
              val totalAsymptomatic = fields(14).trim.toInt
              val currentConfirmed = fields(5).trim.toInt

              val dateParts = fields(15).split("-")
              val year = dateParts(0).toInt
              val month = dateParts(1).toInt
              val day = dateParts(2).toInt

              // 查询 region_id
              selectRegionStmt.setString(1, province)
              selectRegionStmt.setString(2, city)
              val rs1 = selectRegionStmt.executeQuery()
              var regionIdOpt: Option[Int] = None
              if (rs1.next()) regionIdOpt = Some(rs1.getInt("region_id"))
              rs1.close()

              // 查询 date_id
              selectDateStmt.setInt(1, year)
              selectDateStmt.setInt(2, month)
              selectDateStmt.setInt(3, day)
              val rs2 = selectDateStmt.executeQuery()
              var dateIdOpt: Option[Int] = None
              if (rs2.next()) dateIdOpt = Some(rs2.getInt("date_id"))
              rs2.close()

              // 插入 historical_stats
              if (regionIdOpt.isDefined && dateIdOpt.isDefined) {
                insertStatsStmt.setInt(1, regionIdOpt.get)
                insertStatsStmt.setInt(2, dateIdOpt.get)
                insertStatsStmt.setInt(3, totalConfirmed)
                insertStatsStmt.setInt(4, totalDeaths)
                insertStatsStmt.setInt(5, totalRecovered)
                insertStatsStmt.setInt(6, totalImported)
                insertStatsStmt.setInt(7, totalAsymptomatic)
                insertStatsStmt.setInt(8, currentConfirmed)

                insertStatsStmt.executeUpdate()
                println(s"Inserted historical_stats for $province $city on $year-$month-$day")
              } else {
                println(s"Failed to find region/date ID for: $province $city $year-$month-$day")
              }
            }

            insertStatsStmt.close()
            selectRegionStmt.close()
            selectDateStmt.close()
          } catch {
            case e: Exception => e.printStackTrace()
          } finally {
            if (connection != null) connection.close()
          }
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}
