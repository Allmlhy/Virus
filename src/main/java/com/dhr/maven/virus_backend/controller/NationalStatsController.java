package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.dto.NationalStatsDTO;
import com.dhr.maven.virus_backend.service.NationalStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class NationalStatsController {

    @Autowired
    private NationalStatsService nationalStatsService;

    @GetMapping("/national")
    public NationalStatsDTO getNationalStats() {
        return nationalStatsService.getNationalStats();
    }

    @PostMapping("/national/update")
    public String updateNationalStats() {
        nationalStatsService.updateNationalStats();
        return "更新成功";
    }
}
