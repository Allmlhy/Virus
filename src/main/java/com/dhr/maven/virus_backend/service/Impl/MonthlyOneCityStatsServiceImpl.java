package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.repository.MonthlyOneCityStatsRepository;
import com.dhr.maven.virus_backend.service.MonthlyOneCityStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MonthlyOneCityStatsServiceImpl implements MonthlyOneCityStatsService {

    @Autowired
    private MonthlyOneCityStatsRepository repository;

    @Override
    public List<Map<String, Object>> getMonthlySummary(String startMonth, String endMonth, String province, String city) {
        return repository.findMonthlySummary(startMonth, endMonth, province, city);
    }

}
