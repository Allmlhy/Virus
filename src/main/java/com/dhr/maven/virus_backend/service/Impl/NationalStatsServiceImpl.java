package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.dto.NationalStatsDTO;
import com.dhr.maven.virus_backend.pojo.HistoricalStats;
import com.dhr.maven.virus_backend.repository.HistoricalStatsRepositoryBackup;
import com.dhr.maven.virus_backend.repository.DailyStatsRepository;
import com.dhr.maven.virus_backend.service.NationalStatsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NationalStatsServiceImpl implements NationalStatsService {

    private static final String REDIS_KEY = "national_stats";

    @Autowired
    private HistoricalStatsRepositoryBackup historicalStatsRepository;

    @Autowired
    private DailyStatsRepository dailyStatsRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NationalStatsDTO getNationalStats() {
        try {
            String json = redisTemplate.opsForValue().get(REDIS_KEY);
            if (json != null) {
                return objectMapper.readValue(json, NationalStatsDTO.class);
            } else {
                // 如果缓存不存在，则触发更新
                updateNationalStats();
                String updatedJson = redisTemplate.opsForValue().get(REDIS_KEY);
                return updatedJson != null ? objectMapper.readValue(updatedJson, NationalStatsDTO.class) : null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Redis读取或解析失败", e);
        }
    }

    @Override
    public void updateNationalStats() {
        List<HistoricalStats> latestStats = historicalStatsRepository.findLatestStatsByRegion();
        Map<String, Integer> totalMap = new HashMap<>();
        totalMap.put("totalConfirmed", latestStats.stream().mapToInt(HistoricalStats::getTotalConfirmed).sum());
        totalMap.put("totalDeaths", latestStats.stream().mapToInt(HistoricalStats::getTotalDeaths).sum());
        totalMap.put("totalRecovered", latestStats.stream().mapToInt(HistoricalStats::getTotalRecovered).sum());
        totalMap.put("totalImported", latestStats.stream().mapToInt(HistoricalStats::getTotalImported).sum());

        Map<String, Object> newMap = dailyStatsRepository.findLatestNationalDailyStats();

        NationalStatsDTO dto = new NationalStatsDTO();
        dto.setTotalConfirmed(totalMap.get("totalConfirmed"));
        dto.setTotalDeaths(totalMap.get("totalDeaths"));
        dto.setTotalRecovered(totalMap.get("totalRecovered"));
        dto.setTotalImported(totalMap.get("totalImported"));

        dto.setNewConfirmed(((Number) newMap.get("newConfirmed")).intValue());
        dto.setNewDeaths(((Number) newMap.get("newDeaths")).intValue());
        dto.setNewRecovered(((Number) newMap.get("newRecovered")).intValue());
        dto.setNewSuspected(((Number) newMap.get("newSuspected")).intValue());

        try {
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(REDIS_KEY, json);
        } catch (Exception e) {
            throw new RuntimeException("写入Redis失败", e);
        }
    }
}
