package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.repository.RegionCompareBarChartRepository;
import com.dhr.maven.virus_backend.service.RegionCompareBarChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RegionCompareBarChartServiceImpl implements RegionCompareBarChartService {

    @Autowired
    private RegionCompareBarChartRepository repo;

    @Override
    public List<Map<String, Object>> getDailyNewConfirmedByRegion(Integer year, Integer month, Integer day, String regionLevel) {
        return repo.findDailyNewConfirmedByRegion(year, month, day, regionLevel);
    }

    @Override
    public List<Map<String, Object>> getCumulativeStatsByRegion(Integer year, Integer month, Integer day, String regionLevel) {
        return repo.findCumulativeStatsByRegion(year, month, day, regionLevel);
    }
}
