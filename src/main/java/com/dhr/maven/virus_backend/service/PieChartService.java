package com.dhr.maven.virus_backend.service;
import java.util.Map;

public interface PieChartService {
    Map<String, Double> calculateRates(Integer year, Integer month, Integer day, String province, String city);
}
