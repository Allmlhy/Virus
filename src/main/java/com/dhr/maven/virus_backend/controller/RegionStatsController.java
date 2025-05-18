package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.dto.HistoricalStatDTO;
import com.dhr.maven.virus_backend.service.RegionStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.dhr.maven.virus_backend.dto.DailyStatDTO;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/region-stats")
public class RegionStatsController {

    @Autowired
    private RegionStatsService service;

    @GetMapping("/history")
    public List<HistoricalStatDTO> getHistoryByProvinceAndCity(
            @RequestParam String province,
            @RequestParam String city) {
        return service.getHistoricalStats(province, city);
    }

    @GetMapping("/daily")
    public List<DailyStatDTO> getDailyStatsByProvinceAndCity(
            @RequestParam String province,
            @RequestParam String city) {
        return service.getDailyStats(province, city);
    }
}
