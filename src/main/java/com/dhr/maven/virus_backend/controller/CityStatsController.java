package com.dhr.maven.virus_backend.controller;
import com.dhr.maven.virus_backend.service.CityStatsService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import com.dhr.maven.virus_backend.dto.CityStatsDTO;


import java.util.List;


@RestController
@RequestMapping("/api/stats")
public class CityStatsController {
    @Autowired
    private CityStatsService service;

    @GetMapping("/city")
    public List<CityStatsDTO> getStats(@RequestParam String province) {
        return service.getStatsByProvince(province);
    }
}
