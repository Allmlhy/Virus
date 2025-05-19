package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.repository.PieChartRepository;
import com.dhr.maven.virus_backend.service.PieChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PieChartServiceImpl implements PieChartService {

    @Autowired
    private PieChartRepository pieChartRepository;

    @Override
    public Map<String, Double> calculateRates(Integer year, Integer month, Integer day, String province, String city) {
        int totalConfirmed = pieChartRepository.getTotalConfirmed(year, month, day, province, city);
        int totalDeaths = pieChartRepository.getTotalDeaths(year, month, day, province, city);
        int totalRecovered = pieChartRepository.getTotalRecovered(year, month, day, province, city);

        Map<String, Double> result = new HashMap<>();
        if (totalConfirmed == 0) {
            result.put("死亡率", 0.0);
            result.put("治愈率", 0.0);
            result.put("在治率", 0.0);
        } else {
            result.put("死亡率", totalDeaths * 1.0 / totalConfirmed);
            result.put("治愈率", totalRecovered * 1.0 / totalConfirmed);
            result.put("在治率", 1 - result.get("死亡率") - result.get("治愈率"));
        }

        return result;
    }
}
