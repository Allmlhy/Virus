package com.dhr.maven.virus_backend.service;

import com.dhr.maven.virus_backend.dto.CityStatsDTO;

import java.util.List;

public interface CityStatsService {
    List<CityStatsDTO> getStatsByProvince(String province);
}
