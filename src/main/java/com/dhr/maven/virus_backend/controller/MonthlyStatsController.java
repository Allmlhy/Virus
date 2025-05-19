package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.MonthlyStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barchart/monthly_wholechina")
public class MonthlyStatsController {

    @Autowired
    private MonthlyStatsService monthlyStatsService;

    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getMonthlySummary(
            @RequestParam(required = false) String startMonth, // yyyy-MM 格式，例如 "2023-01"
            @RequestParam(required = false) String endMonth    // yyyy-MM 格式，例如 "2023-04"
    ) {
        List<Map<String, Object>> list = monthlyStatsService.getMonthlySummary(startMonth, endMonth);
        return ResponseEntity.ok(list);
    }
}
