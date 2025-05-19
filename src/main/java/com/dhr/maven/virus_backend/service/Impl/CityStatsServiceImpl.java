package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.dto.CityStatsDTO;
import com.dhr.maven.virus_backend.repository.HistoricalStatsRepositoryBackup;
import com.dhr.maven.virus_backend.service.CityStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CityStatsServiceImpl implements CityStatsService {

    @Autowired
    private HistoricalStatsRepositoryBackup repository;

    @Override
    public List<CityStatsDTO> getStatsByProvince(String province) {
        return repository.findLatestStatsByProvince(province);
    }
}
