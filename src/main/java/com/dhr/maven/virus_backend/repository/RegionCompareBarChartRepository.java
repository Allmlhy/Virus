package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.pojo.HistoricalStats;
import com.dhr.maven.virus_backend.pojo.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface RegionCompareBarChartRepository extends JpaRepository<HistoricalStats, Long> {

    // ✅ 查询每日新增确诊：来自 DailyStats 表
    @Query("SELECT new map(" +
            "CASE WHEN :regionLevel = 'province' THEN r.province ELSE r.city END as region, " +
            "COALESCE(SUM(ds.newConfirmed), 0) as newConfirmed) " +
            "FROM DailyStats ds " +
            "JOIN ds.date d " +
            "JOIN ds.region r " +
            "WHERE d.year = :year AND d.month = :month AND d.day = :day " +
            "GROUP BY CASE WHEN :regionLevel = 'province' THEN r.province ELSE r.city END " )
    List<Map<String, Object>> findDailyNewConfirmedByRegion(@Param("year") Integer year,
                                                            @Param("month") Integer month,
                                                            @Param("day") Integer day,
                                                            @Param("regionLevel") String regionLevel);

    // ✅ 查询累计确诊、累计死亡、现存确诊：来自 HistoricalStats 表
    @Query("SELECT new map(" +
            "CASE WHEN :regionLevel = 'province' THEN r.province ELSE r.city END as region, " +
            "COALESCE(SUM(rs.totalConfirmed), 0) as totalConfirmed, " +
            "COALESCE(SUM(rs.totalDeaths), 0) as totalDeaths, " +
            "COALESCE(SUM(rs.currentConfirmed), 0) as currentConfirmed) " +
            "FROM HistoricalStats rs " +
            "JOIN rs.date d " +
            "JOIN rs.region r " +
            "WHERE d.year = :year AND d.month = :month AND d.day = :day " +
            "GROUP BY CASE WHEN :regionLevel = 'province' THEN r.province ELSE r.city END ")
    List<Map<String, Object>> findCumulativeStatsByRegion(@Param("year") Integer year,
                                                          @Param("month") Integer month,
                                                          @Param("day") Integer day,
                                                          @Param("regionLevel") String regionLevel);
}
