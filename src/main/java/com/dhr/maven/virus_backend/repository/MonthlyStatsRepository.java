package com.dhr.maven.virus_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.dhr.maven.virus_backend.pojo.HistoricalStats;
import java.util.List;
import java.util.Map;

public interface MonthlyStatsRepository extends JpaRepository<HistoricalStats, Long> {

    @Query(value =
            "SELECT DATE_FORMAT(CONCAT(d.year, '-', LPAD(d.month, 2, '0'), '-01'), '%Y-%m') AS month, " +
                    "       SUM(hs.total_confirmed) AS totalConfirmed, " +
                    "       SUM(hs.total_deaths) AS totalDeaths, " +
                    "       SUM(hs.total_recovered) AS totalRecovered " +
                    "FROM historical_stats hs " +
                    "JOIN date_dim d ON hs.date_id = d.date_id " +
                    "JOIN ( " +
                    "    SELECT region_id, year, month, MAX(day) AS max_day " +
                    "    FROM historical_stats hs2 " +
                    "    JOIN date_dim d2 ON hs2.date_id = d2.date_id " +
                    "    WHERE (:startMonth IS NULL OR CONCAT(d2.year, '-', LPAD(d2.month, 2, '0')) >= :startMonth) " +
                    "      AND (:endMonth IS NULL OR CONCAT(d2.year, '-', LPAD(d2.month, 2, '0')) <= :endMonth) " +
                    "    GROUP BY region_id, year, month " +
                    ") last_day ON hs.region_id = last_day.region_id " +
                    "         AND d.year = last_day.year " +
                    "         AND d.month = last_day.month " +
                    "         AND d.day = last_day.max_day " +
                    "GROUP BY month " +
                    "ORDER BY month",
            nativeQuery = true)
    List<Map<String, Object>> findMonthlySummary(
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth);
}
