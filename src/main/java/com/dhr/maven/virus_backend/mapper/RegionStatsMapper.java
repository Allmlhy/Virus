package com.dhr.maven.virus_backend.mapper;

import com.dhr.maven.virus_backend.dto.HistoricalStatDTO;
import com.dhr.maven.virus_backend.dto.DailyStatDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegionStatsMapper {

    @Select("SELECT region_id FROM regions WHERE province = #{province} AND city = #{city}")
    Integer getRegionId(@Param("province") String province, @Param("city") String city);

    @Select("SELECT CONCAT(d.year, '-', LPAD(d.month, 2, '0'), '-', LPAD(d.day, 2, '0')) AS date, " +
            "h.total_confirmed AS totalConfirmed, h.total_deaths AS totalDeaths, " +
            "h.total_recovered AS totalRecovered, h.total_imported AS totalImported, " +
            "h.total_asymptomatic AS totalAsymptomatic, h.current_confirmed AS currentConfirmed " +
            "FROM historical_stats h " +
            "JOIN date_dim d ON h.date_id = d.date_id " +
            "WHERE h.region_id = #{regionId} " +
            "ORDER BY d.year, d.month, d.day")
    List<HistoricalStatDTO> getStatsByRegionId(@Param("regionId") Integer regionId);

    @Select("SELECT CONCAT(d.year, '-', LPAD(d.month, 2, '0'), '-', LPAD(d.day, 2, '0')) AS date, " +
            "ds.new_confirmed AS newConfirmed, ds.new_deaths AS newDeaths, " +
            "ds.new_recovered AS newRecovered, ds.new_suspected AS newSuspected, " +
            "ds.v, ds.a " +
            "FROM daily_stats ds " +
            "JOIN date_dim d ON ds.date_id = d.date_id " +
            "WHERE ds.region_id = #{regionId} " +
            "ORDER BY d.year, d.month, d.day")
    List<DailyStatDTO> getDailyStatsByRegionId(@Param("regionId") Integer regionId);
}
