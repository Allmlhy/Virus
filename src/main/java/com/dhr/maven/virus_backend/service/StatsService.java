package com.dhr.maven.virus_backend.service;

import java.time.LocalDate;
import java.util.Map;

// StatsService.java
public interface StatsService {
    Map<String, Map<String, Integer>> getLatestProvinceDetailedStats();
    Map<String, Map<String, Integer>> getYearlyProvinceDetailedStats(int year);
    Map<String, Map<String, Integer>> getProvinceDetailedStatsByDate(LocalDate date);

}