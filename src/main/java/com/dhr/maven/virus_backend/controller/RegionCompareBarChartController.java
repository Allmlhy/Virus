package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.RegionCompareBarChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/barchart/regioncompare")
public class RegionCompareBarChartController {

    @Autowired
    private RegionCompareBarChartService regionCompareService;

    @GetMapping("/dailyNewConfirmed")
    public ResponseEntity<List<Map<String, Object>>> getDailyNewConfirmedByRegion(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer day,
            @RequestParam(required = false, defaultValue = "province") String regionLevel // province 或 city
    ) {
        List<Map<String, Object>> result = regionCompareService.getDailyNewConfirmedByRegion(year, month, day, regionLevel);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cumulativeCompare")
    public ResponseEntity<List<Map<String, Object>>> getCumulativeCompareByRegion(
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer day,
            @RequestParam(required = false, defaultValue = "province") String regionLevel
    ) {
        List<Map<String, Object>> result = regionCompareService.getCumulativeStatsByRegion(year, month, day, regionLevel);
        return ResponseEntity.ok(result);
    }
}

