package com.dhr.maven.virus_backend.service;

import java.util.List;
import java.util.Map;

public interface TimeSeriesBarChartService {
    List<Map<String, Object>> getDailyStatsWithRates(Integer year, Integer month, String province, String city);
    List<Map<String, Object>> getDailyStatsWholeChina(Integer year, Integer month);
}