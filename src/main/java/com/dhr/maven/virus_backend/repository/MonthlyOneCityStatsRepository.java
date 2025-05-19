package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.pojo.HistoricalStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface MonthlyOneCityStatsRepository extends JpaRepository<HistoricalStats, Long> {

    @Query(value =
            "SELECT DATE_FORMAT(CONCAT(d.year, '-', LPAD(d.month, 2, '0'), '-01'), '%Y-%m') AS month, " +
                    "       SUM(hs.total_confirmed) AS totalConfirmed, " +
                    "       SUM(hs.total_deaths) AS totalDeaths, " +
                    "       SUM(hs.total_recovered) AS totalRecovered " +
                    "FROM historical_stats hs " +
                    "JOIN date_dim d ON hs.date_id = d.date_id " +
                    "JOIN regions r ON hs.region_id = r.region_id " +
                    "JOIN ( " +
                    "    SELECT hs2.region_id, d2.year, d2.month, MAX(d2.day) AS max_day " +
                    "    FROM historical_stats hs2 " +
                    "    JOIN date_dim d2 ON hs2.date_id = d2.date_id " +
                    "    JOIN regions r2 ON hs2.region_id = r2.region_id " +
                    "    WHERE (:startMonth IS NULL OR CONCAT(d2.year, '-', LPAD(d2.month, 2, '0')) >= :startMonth) " +
                    "      AND (:endMonth IS NULL OR CONCAT(d2.year, '-', LPAD(d2.month, 2, '0')) <= :endMonth) " +
                    "      AND r2.province = :province " +
                    "      AND (:city IS NULL OR r2.city = :city) " +
                    "    GROUP BY hs2.region_id, d2.year, d2.month " +
                    ") last_day ON hs.region_id = last_day.region_id " +
                    "          AND d.year = last_day.year " +
                    "          AND d.month = last_day.month " +
                    "          AND d.day = last_day.max_day " +
                    "WHERE r.province = :province " +
                    "  AND (:city IS NULL OR r.city = :city) " +
                    "GROUP BY month " +
                    "ORDER BY month",
            nativeQuery = true)
    List<Map<String, Object>> findMonthlySummary(
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth,
            @Param("province") String province,
            @Param("city") String city);

}
