package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.pojo.DateDim;
import com.dhr.maven.virus_backend.pojo.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {

    @Query("SELECT d FROM DailyStats d " +
            "WHERE d.date.dateId = :dateId " +
            "AND d.region.regionId = :regionId")
    Optional<DailyStats> findByDateAndRegion(
            @Param("dateId") Long dateId,
            @Param("regionId") Integer regionId
    );

    @Query("SELECT d FROM DailyStats d WHERE d.date.dateId = :dateId")
    List<DailyStats> findByDate(@Param("dateId") Long dateId);

    @Query("SELECT d FROM DailyStats d WHERE d.region.regionId = :regionId")
    List<DailyStats> findByRegion(@Param("regionId") Integer regionId);

    @Query("SELECT d FROM DailyStats d " +
            "WHERE d.date.dateId IN :dateIds " +
            "AND d.region.regionId IN :regionIds " +
            "ORDER BY d.date.dateId DESC")
    List<DailyStats> findBatchByDatesAndRegions(
            @Param("dateIds") List<Long> dateIds,
            @Param("regionIds") List<Integer> regionIds
    );

    @Query("SELECT r.province, dd.dateId, SUM(d.newConfirmed) " +
            "FROM DailyStats d " +
            "JOIN d.region r " +
            "JOIN d.date dd " +
            "WHERE dd.dateId BETWEEN :startDateId AND :endDateId " +
            "AND r.level = 0 " +
            "GROUP BY r.province, dd.dateId " +
            "ORDER BY dd.dateId ASC")
    List<Object[]> findTrendByDateRangeAndProvince(
            @Param("startDateId") Long startDateId,
            @Param("endDateId") Long endDateId
    );

    // 👇 增加聚合查询
    @Query("SELECT COALESCE(SUM(d.newConfirmed), 0) " +
            "FROM DailyStats d " +
            "WHERE d.date.dateId = :dateId AND d.region.regionId IN :regionIds")
    Optional<Integer> findNewConfirmedByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );

    @Query("SELECT COALESCE(SUM(d.newDeaths), 0) " +
            "FROM DailyStats d " +
            "WHERE d.date.dateId = :dateId AND d.region.regionId IN :regionIds")
    Optional<Integer> findNewDeadByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );

    @Query("SELECT COALESCE(SUM(d.newRecovered), 0) " +
            "FROM DailyStats d " +
            "WHERE d.date.dateId = :dateId AND d.region.regionId IN :regionIds")
    Optional<Integer> findNewCuredByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );

    @Query("SELECT COALESCE(SUM(d.newSuspected), 0) " +
            "FROM DailyStats d " +
            "WHERE d.date.dateId = :dateId AND d.region.regionId IN :regionIds")
    Optional<Integer> findNewSuspectedByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );
    /**
     * 全国每日新增数据聚合
     */
    @Query(value = "SELECT " +
            "SUM(new_confirmed) AS newConfirmed, " +
            "SUM(new_deaths) AS newDeaths, " +
            "SUM(new_recovered) AS newRecovered, " +
            "SUM(new_suspected) AS newSuspected " +
            "FROM daily_stats " +
            "WHERE (region_id, date_id) IN ( " +
            "    SELECT region_id, MAX(date_id) " +
            "    FROM daily_stats " +
            "    GROUP BY region_id " +
            ")", nativeQuery = true)
    Map<String, Object> findLatestNationalDailyStats();


}
