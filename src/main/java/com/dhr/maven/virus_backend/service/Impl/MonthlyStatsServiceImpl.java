package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.repository.MonthlyStatsRepository;
import com.dhr.maven.virus_backend.service.MonthlyStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MonthlyStatsServiceImpl implements MonthlyStatsService {

    @Autowired
    private MonthlyStatsRepository repository;

    @Override
    public List<Map<String, Object>> getMonthlySummary(String startMonth, String endMonth) {
        return repository.findMonthlySummary(startMonth, endMonth);
    }
}
