import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.sql.SparkSession

import java.sql.{Connection, DriverManager, ResultSet}

object CovidCityExtractor {
  def start(): Unit = {
    val conf = new SparkConf().setAppName("CovidCityExtractor").setMaster("local[*]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "covid-city-group",
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    val topics = Array("china-covid-raw")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
    )

    val spark = SparkSession.builder().config(conf).getOrCreate()
    import spark.implicits._

    stream.foreachRDD { rdd =>
      if (!rdd.isEmpty()) {
        val lines = rdd.map(record => record.value())

        val rowRDD = lines
          .map(_.split(",", -1))
          .filter(fields => fields.length >= 3 && fields(0) == "中国")
          .map(fields => (fields(1).trim, fields(2).trim)) // (province, city)

        val distinctRecords = rowRDD.distinct().collect()

        if (distinctRecords.nonEmpty) {
          // JDBC连接信息
          val url = "jdbc:mysql://106.12.170.52:13327/sparkprogram?allowPublicKeyRetrieval=true&useSSL=false"
          val user = "lmx"
          val password = "lmx"

          var connection: Connection = null
          try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            connection = DriverManager.getConnection(url, user, password)
            val stmt = connection.createStatement()

            // 读取已存在的 (province, city) 对，防止重复插入
            val existingSet = scala.collection.mutable.Set[String]()
            val rs2: ResultSet = stmt.executeQuery("SELECT province, city FROM regions")
            while (rs2.next()) {
              val key = rs2.getString("province") + "-" + rs2.getString("city")
              existingSet += key
            }
            rs2.close()

            // 过滤出数据库中没有的记录
            val newRecords = distinctRecords.filter { case (prov, city) =>
              val key = prov + "-" + city
              !existingSet.contains(key)
            }

            if (newRecords.isEmpty) {
              println("No new records to insert.")
            } else {
              // 省份排序，并分配省份ID
              val provinces = newRecords.map(_._1).distinct.sorted
              val provinceIdMap = provinces.zipWithIndex.map { case (prov, idx) => (prov, idx + 1) }.toMap

              // 对每个省，进行市的排序：
              // 先正常市（不以“境外”“未”开头）升序，再“境外”“未”开头的市
              val grouped = newRecords.groupBy(_._1).map { case (prov, cityList) =>
                val normalCities = cityList.filter { case (_, city) =>
                  !city.startsWith("境外") && !city.startsWith("未")
                }.map(_._2).distinct.sorted

                val specialCities = cityList.filter { case (_, city) =>
                  city.startsWith("境外") || city.startsWith("未")
                }.map(_._2).distinct.sorted

                val orderedCities = normalCities ++ specialCities
                (prov, orderedCities)
              }

              // 准备插入数据
              val insertSql = "INSERT INTO regions (region_id, province, city, level) VALUES (?, ?, ?, ?)"
              val pstmt = connection.prepareStatement(insertSql)

              for ((prov, cities) <- grouped.toSeq.sortBy(_._1)) {
                val provId = provinceIdMap(prov)
                cities.zipWithIndex.foreach { case (city, idx) =>
                  val key = prov + "-" + city
                  if (!existingSet.contains(key)) {
                    val cityId = idx + 1
                    val regionId = provId * 1000 + cityId // 生成唯一region_id
                    pstmt.setInt(1, regionId)
                    pstmt.setString(2, prov)
                    pstmt.setString(3, city)
                    pstmt.setInt(4, 0) // level默认0
                    pstmt.executeUpdate()
                    println(s"Inserted region: $prov $city with region_id = $regionId")
                  }
                }
              }

              pstmt.close()
            }

            stmt.close()
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
