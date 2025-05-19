package com.dhr.maven.virus_backend.service;

import java.util.List;
import java.util.Map;

public interface MonthlyStatsService {
    List<Map<String, Object>> getMonthlySummary(String startMonth, String endMonth);
}
