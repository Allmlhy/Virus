import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

object RegionLevelUpdater {

  def main(args: Array[String]): Unit = {
    updateRegionLevels()
  }

  def updateRegionLevels(): Unit = {
    // 1. JDBC连接信息
    val url = "jdbc:mysql://106.12.170.52:13327/sparkprogram?allowPublicKeyRetrieval=true&useSSL=false"
    val user = "lmx"
    val password = "lmx"
    var conn: Connection = null

    try {
      conn = DriverManager.getConnection(url, user, password)

      // 2. 获取每个 region_id 最新 date_id 的 total_confirmed
      val query =
        """
          |SELECT hs.region_id, hs.total_confirmed
          |FROM historical_stats hs
          |JOIN (
          |    SELECT region_id, MAX(date_id) AS max_date
          |    FROM historical_stats
          |    GROUP BY region_id
          |) latest
          |ON hs.region_id = latest.region_id AND hs.date_id = latest.max_date
          |""".stripMargin

      val stmt = conn.createStatement()
      val rs: ResultSet = stmt.executeQuery(query)

      // 3. 准备更新语句
      val updateStmt: PreparedStatement = conn.prepareStatement("UPDATE regions SET level = ? WHERE region_id = ?")

      // 4. 遍历每个城市的 total_confirmed，计算 level 并更新
      while (rs.next()) {
        val regionId = rs.getInt("region_id")
        val confirmed = rs.getInt("total_confirmed")

        val level = confirmed match {
          case c if c == 0 => 0
          case c if c >= 1 && c <= 100 => 1
          case c if c <= 1000 => 2
          case c if c <= 10000 => 3
          case c if c <= 100000 => 4
          case _ => 5
        }

        updateStmt.setInt(1, level)
        updateStmt.setInt(2, regionId)
        updateStmt.executeUpdate()
      }

      // 关闭资源
      rs.close()
      stmt.close()
      updateStmt.close()

      println("Region levels updated successfully.")
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (conn != null) conn.close()
    }
  }
}
