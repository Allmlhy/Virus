package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.PieChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/piechart")
public class PieChartController {

    @Autowired
    private PieChartService pieChartService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Double>> getPieChartData(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day,
            @RequestParam(required = false, defaultValue = "全国") String province,
            @RequestParam(required = false) String city) {

        Map<String, Double> result = pieChartService.calculateRates(year, month, day, province, city);
        return ResponseEntity.ok(result);
    }
}
