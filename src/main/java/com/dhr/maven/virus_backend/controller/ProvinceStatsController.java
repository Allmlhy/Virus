package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// ProvinceStatsController.java
@RestController
@RequestMapping("/api/stats")
public class ProvinceStatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/province")
    public ResponseEntity<?> getProvinceStats(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        if (date == null && year == null) {
            return ResponseEntity.ok(statsService.getLatestProvinceDetailedStats());
        } else if (date != null) {
            return ResponseEntity.ok(statsService.getProvinceDetailedStatsByDate(date));
        } else {
            return ResponseEntity.ok(statsService.getYearlyProvinceDetailedStats(year));
        }
    }

}