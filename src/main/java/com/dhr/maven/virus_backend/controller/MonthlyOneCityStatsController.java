package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.MonthlyOneCityStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barchart/monthly_onecity")
public class MonthlyOneCityStatsController {

    @Autowired
    private MonthlyOneCityStatsService monthlyOneCityStatsService;

    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getMonthlySummary(
            @RequestParam(required = false) String startMonth,  // yyyy-MM
            @RequestParam(required = false) String endMonth,    // yyyy-MM
            @RequestParam String province,                       // 省名
            @RequestParam(required = false) String city         // 市名，可选
    ) {
        List<Map<String, Object>> list = monthlyOneCityStatsService.getMonthlySummary(startMonth, endMonth, province, city);
        return ResponseEntity.ok(list);
    }

}
