package com.dhr.maven.virus_backend.service;

import java.util.*;


public interface RegionCompareBarChartService {
    List<Map<String, Object>> getDailyNewConfirmedByRegion(Integer year, Integer month, Integer day, String regionLevel);
    List<Map<String, Object>> getCumulativeStatsByRegion(Integer year, Integer month, Integer day, String regionLevel);
}

