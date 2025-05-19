package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.pojo.HistoricalStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PieChartRepository extends JpaRepository<HistoricalStats, Long> {

    @Query("SELECT COALESCE(SUM(rs.totalConfirmed), 0) " +
            "FROM HistoricalStats rs " +
            "JOIN rs.region r " +
            "JOIN rs.date d " +
            "WHERE (:year IS NULL OR d.year = :year) " +
            "AND (:month IS NULL OR d.month = :month) " +
            "AND (:day IS NULL OR d.day = :day) " +
            "AND (:province = '全国' OR r.province = :province) " +
            "AND (:city IS NULL OR r.city = :city)")
    int getTotalConfirmed(@Param("year") Integer year,
                          @Param("month") Integer month,
                          @Param("day") Integer day,
                          @Param("province") String province,
                          @Param("city") String city);

    @Query("SELECT COALESCE(SUM(rs.totalDeaths), 0) " +
            "FROM HistoricalStats rs " +
            "JOIN rs.region r " +
            "JOIN rs.date d " +
            "WHERE (:year IS NULL OR d.year = :year) " +
            "AND (:month IS NULL OR d.month = :month) " +
            "AND (:day IS NULL OR d.day = :day) " +
            "AND (:province = '全国' OR r.province = :province) " +
            "AND (:city IS NULL OR r.city = :city)")
    int getTotalDeaths(@Param("year") Integer year,
                       @Param("month") Integer month,
                       @Param("day") Integer day,
                       @Param("province") String province,
                       @Param("city") String city);

    @Query("SELECT COALESCE(SUM(rs.totalRecovered), 0) " +
            "FROM HistoricalStats rs " +
            "JOIN rs.region r " +
            "JOIN rs.date d " +
            "WHERE (:year IS NULL OR d.year = :year) " +
            "AND (:month IS NULL OR d.month = :month) " +
            "AND (:day IS NULL OR d.day = :day) " +
            "AND (:province = '全国' OR r.province = :province) " +
            "AND (:city IS NULL OR r.city = :city)")
    int getTotalRecovered(@Param("year") Integer year,
                          @Param("month") Integer month,
                          @Param("day") Integer day,
                          @Param("province") String province,
                          @Param("city") String city);
}
