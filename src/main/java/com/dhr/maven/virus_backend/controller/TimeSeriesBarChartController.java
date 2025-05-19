package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.TimeSeriesBarChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barchart/timeseries")
public class TimeSeriesBarChartController {

    @Autowired
    private TimeSeriesBarChartService timeSeriesService;

    // 获取每日新增和增长率加速度
    @GetMapping("/dailyStats")
    public ResponseEntity<List<Map<String, Object>>> getDailyStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false, defaultValue = "全国") String province,
            @RequestParam(required = false) String city) {

        List<Map<String, Object>> list = timeSeriesService.getDailyStatsWithRates(year, month, province, city);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/dailyStats_wholechina")
    public ResponseEntity<List<Map<String, Object>>> getDailyStatsWholeChina(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        List<Map<String, Object>> list = timeSeriesService.getDailyStatsWholeChina(year, month);
        return ResponseEntity.ok(list);
    }

}
