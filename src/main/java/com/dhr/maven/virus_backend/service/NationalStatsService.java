package com.dhr.maven.virus_backend.service;

import com.dhr.maven.virus_backend.dto.NationalStatsDTO;

public interface NationalStatsService {
    NationalStatsDTO getNationalStats();
    void updateNationalStats();  // 每日调度更新用
}
