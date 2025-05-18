package com.dhr.maven.virus_backend.service;

import com.dhr.maven.virus_backend.dto.HistoricalStatDTO;
import com.dhr.maven.virus_backend.dto.DailyStatDTO;
import com.dhr.maven.virus_backend.mapper.RegionStatsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RegionStatsService {

    @Autowired
    private RegionStatsMapper mapper;

    public List<HistoricalStatDTO> getHistoricalStats(String province, String city) {
        Integer regionId = mapper.getRegionId(province, city);
        if (regionId == null) {
            return null;
        }
        return mapper.getStatsByRegionId(regionId);
    }

    public List<DailyStatDTO> getDailyStats(String province, String city) {
        Integer regionId = mapper.getRegionId(province, city);
        if (regionId == null) {
            return new ArrayList<>();
        }
        return mapper.getDailyStatsByRegionId(regionId);
    }
}
