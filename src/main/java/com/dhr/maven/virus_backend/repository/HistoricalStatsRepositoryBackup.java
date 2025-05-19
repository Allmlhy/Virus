package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.dto.CityStatsDTO;
import com.dhr.maven.virus_backend.pojo.HistoricalStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HistoricalStatsRepositoryBackup extends JpaRepository<HistoricalStats, Integer> {

    @Query("SELECT COALESCE(SUM(h.totalConfirmed), 0) " +
            "FROM HistoricalStats h " +
            "WHERE h.date.dateId = :dateId AND h.region.regionId IN :regionIds")
    Optional<Integer> findTotalConfirmedByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );

    @Query("SELECT COALESCE(SUM(h.totalDeaths), 0) " +
            "FROM HistoricalStats h " +
            "WHERE h.date.dateId = :dateId AND h.region.regionId IN :regionIds")
    Optional<Integer> findTotalDeadByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );

    @Query("SELECT COALESCE(SUM(h.totalRecovered), 0) " +
            "FROM HistoricalStats h " +
            "WHERE h.date.dateId = :dateId AND h.region.regionId IN :regionIds")
    Optional<Integer> findTotalCuredByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );

    @Query("SELECT COALESCE(SUM(h.totalImported), 0) " +
            "FROM HistoricalStats h " +
            "WHERE h.date.dateId = :dateId AND h.region.regionId IN :regionIds")
    Optional<Integer> findTotalImportedByDateAndRegions(
            @Param("dateId") Integer dateId,
            @Param("regionIds") List<Integer> regionIds
    );
    /**
     * 获取各区域最新日期的统计数据
     */
    @Query(value = "SELECT h1.* FROM historical_stats h1 " +
            "WHERE h1.date_id = (SELECT MAX(h2.date_id) FROM historical_stats h2 WHERE h2.region_id = h1.region_id)",
            nativeQuery = true)
    List<HistoricalStats> findLatestStatsByRegion();

    @Query(value = "SELECT " +
            "SUM(total_confirmed) AS totalConfirmed, " +
            "SUM(total_deaths) AS totalDeaths, " +
            "SUM(total_recovered) AS totalRecovered, " +
            "SUM(total_imported) AS totalImported " +
            "FROM historical_stats " +
            "WHERE (region_id, date_id) IN ( " +
            "    SELECT region_id, MAX(date_id) " +
            "    FROM historical_stats " +
            "    GROUP BY region_id " +
            ")",
            nativeQuery = true)
    Map<String, Object> findLatestNationalTotal();

    @Query("SELECT new com.dhr.maven.virus_backend.dto.CityStatsDTO(" +
            "hs.region.city, " +
            "hs.totalConfirmed, " +
            "hs.totalDeaths, " +
            "hs.totalRecovered, " +
            "hs.totalImported, " +
            "hs.totalAsymptomatic, " +
            "hs.currentConfirmed) " +
            "FROM HistoricalStats hs " +
            "WHERE hs.region.province = :province " +
            "AND hs.date.dateId = (SELECT MAX(h.date.dateId) FROM HistoricalStats h) " +
            "ORDER BY hs.region.city")
    List<CityStatsDTO> findLatestStatsByProvince(@Param("province") String province);


    /**
     * 全国累计数据聚合
     */
    default Map<String, Integer> getNationalTotal() {
        Map<String, Object> raw = findLatestNationalTotal();

        Map<String, Integer> result = new HashMap<>();
        result.put("totalConfirmed", ((Number) raw.get("totalConfirmed")).intValue());
        result.put("totalDeaths", ((Number) raw.get("totalDeaths")).intValue());
        result.put("totalRecovered", ((Number) raw.get("totalRecovered")).intValue());
        result.put("totalImported", ((Number) raw.get("totalImported")).intValue());
        return result;
    }

}
