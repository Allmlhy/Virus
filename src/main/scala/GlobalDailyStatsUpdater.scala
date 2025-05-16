import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}
import java.util.concurrent.{Executors, TimeUnit}

object GlobalDailyStatsUpdater {

  // 数据库连接配置
  val jdbcUrl = "jdbc:mysql://106.12.170.52:13327/sparkprogram?characterEncoding=utf8&useSSL=false"
  val jdbcUser = "lmx"
  val jdbcPassword = "lmx"

  def main(args: Array[String]): Unit = {
    // 创建调度线程池
    val scheduler = Executors.newScheduledThreadPool(1)

    val updateTask = new Runnable {
      override def run(): Unit = {
        var conn: Connection = null
        try {
          println("[INFO] 定时任务开始，更新字段 a ...")
          conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword)
          println("[DEBUG] 数据库连接成功")
          conn.setAutoCommit(false)

          val selectSql =
            """
              |SELECT country_code, date_id, v
              |FROM global_daily_stats
              |ORDER BY country_code, date_id
              |""".stripMargin
          println(s"[DEBUG] 执行查询SQL:\n$selectSql")

          val stmt = conn.createStatement()
          val rs = stmt.executeQuery(selectSql)

          val updateSql = "UPDATE global_daily_stats SET a = ? WHERE country_code = ? AND date_id = ?"
          val pstmt: PreparedStatement = conn.prepareStatement(updateSql)

          var lastCountry: String = null
          var prevV = 0.0
          val batchSize = 1000
          var batchCounter = 0
          var updateCount = 0

          while (rs.next()) {
            val country = rs.getString("country_code")
            val dateId = rs.getInt("date_id")
            val v = rs.getDouble("v")

            if (lastCountry != null && lastCountry != country) {
              // 国家变了，prevV重置
              prevV = 0.0
            }

            val a = v - prevV

            pstmt.setDouble(1, a)
            pstmt.setString(2, country)
            pstmt.setInt(3, dateId)
            pstmt.addBatch()

            prevV = v
            lastCountry = country
            batchCounter += 1
            updateCount += 1

            if (batchCounter >= batchSize) {
              pstmt.executeBatch()
              conn.commit()
              pstmt.clearBatch()
              println(s"[DEBUG] 已执行并提交 $updateCount 条更新")
              batchCounter = 0
            }
          }

          // 执行剩余批次
          if (batchCounter > 0) {
            pstmt.executeBatch()
            conn.commit()
            pstmt.clearBatch()
            println(s"[DEBUG] 已执行并提交剩余 $batchCounter 条更新")
          }

          pstmt.close()
          rs.close()
          stmt.close()

          println(s"[INFO] 定时任务完成，共更新 $updateCount 条记录的字段 a。")

        } catch {
          case e: Exception =>
            println(s"[ERROR] 定时任务执行异常: ${e.getMessage}")
            e.printStackTrace()
        } finally {
          if (conn != null && !conn.isClosed) {
            conn.close()
            println("[DEBUG] 数据库连接关闭")
          }
        }
      }
    }

    scheduler.scheduleAtFixedRate(updateTask, 10, 30, TimeUnit.SECONDS)

    println("[INFO] 定时更新任务已启动，按周期执行...")
  }
}
