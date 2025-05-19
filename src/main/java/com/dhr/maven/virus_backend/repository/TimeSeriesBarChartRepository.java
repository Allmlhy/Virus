package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.pojo.DailyStats;
import com.dhr.maven.virus_backend.pojo.HistoricalStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface TimeSeriesBarChartRepository extends JpaRepository<DailyStats, Long> {

    // 按地区查每日数据
    @Query("SELECT new map(d.day as day, " +
            "COALESCE(SUM(ds.newConfirmed), 0) as newConfirmed, " +
            "COALESCE(SUM(ds.newDeaths), 0) as newDeaths, " +
            "COALESCE(SUM(ds.newRecovered), 0) as newRecovered) " +
            "FROM DailyStats ds " +
            "JOIN ds.date d " +
            "JOIN ds.region r " +
            "WHERE (:year IS NULL OR d.year = :year) " +
            "AND (:month IS NULL OR d.month = :month) " +
            "AND (:province = '全国' OR r.province = :province) " +
            "AND (:city IS NULL OR r.city = :city) " +
            "GROUP BY d.day " +
            "ORDER BY d.day")
    List<Map<String, Object>> findDailyStats(
            @Param("year") Integer year,
            @Param("month") Integer month,
            @Param("province") String province,
            @Param("city") String city);

    // 查询全国每日总新增确诊
    @Query("SELECT new map(d.day as day, " +
            "COALESCE(SUM(ds.newConfirmed), 0) as newConfirmed, " +
            "COALESCE(SUM(ds.newDeaths), 0) as newDeaths, " +
            "COALESCE(SUM(ds.newRecovered), 0) as newRecovered) " +
            "FROM DailyStats ds " +
            "JOIN ds.date d " +
            "JOIN ds.region r " +
            "WHERE (:year IS NULL OR d.year = :year) " +
            "AND (:month IS NULL OR d.month = :month) " +
            "GROUP BY d.day " +
            "ORDER BY d.day")
    List<Map<String, Object>> findDailyStatsWholeChina(
            @Param("year") Integer year,
            @Param("month") Integer month);




}

