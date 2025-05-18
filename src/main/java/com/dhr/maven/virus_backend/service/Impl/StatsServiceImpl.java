package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.exception.DataNotFoundException;
import com.dhr.maven.virus_backend.pojo.DateDim;
import com.dhr.maven.virus_backend.pojo.Region;
import com.dhr.maven.virus_backend.repository.DailyStatsRepository;
import com.dhr.maven.virus_backend.repository.DateDimRepository;
import com.dhr.maven.virus_backend.repository.HistoricalStatsRepository;
import com.dhr.maven.virus_backend.repository.RegionRepository;
import com.dhr.maven.virus_backend.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StatsServiceImpl implements StatsService {

    private static final Logger logger = LoggerFactory.getLogger(StatsServiceImpl.class);

    private static final String PROVINCE_STATS_KEY = "province_stats:";
    private static final String LATEST_STATS_KEY = "latest_stats";
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private DateDimRepository dateDimRepository;

    @Autowired
    private HistoricalStatsRepository historicalStatsRepository;

    @Autowired
    private DailyStatsRepository dailyStatsRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Map<String, Map<String, Integer>> getLatestProvinceDetailedStats() {
        // 从缓存获取
        String cacheKey = LATEST_STATS_KEY + ":detailed";
        Map<String, Map<String, Integer>> cachedResult = (Map<String, Map<String, Integer>>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            logger.info("Cache hit for latest detailed province stats");
            return cachedResult;
        }

        DateDim latestDate = dateDimRepository.findTopByOrderByDateIdDesc()
                .orElseThrow(() -> new DataNotFoundException("No data available"));

        return getAndCacheDetailedProvinceStats(latestDate, cacheKey);
    }

    @Override
    @Cacheable(value = "provinceStatsDetailed", key = "#year")
    public Map<String, Map<String, Integer>> getYearlyProvinceDetailedStats(int year) {
        DateDim lastDate = dateDimRepository.findTopByYearOrderByDateIdDesc(year)
                .orElseThrow(() -> new DataNotFoundException("No data for year: " + year));
        String cacheKey = PROVINCE_STATS_KEY + year + ":detailed";
        return getAndCacheDetailedProvinceStats(lastDate, cacheKey);
    }


    @Override
    public Map<String, Map<String, Integer>> getProvinceDetailedStatsByDate(LocalDate date) {
        String cacheKey = PROVINCE_STATS_KEY + date.toString() + ":detailed";
        Map<String, Map<String, Integer>> cachedResult = (Map<String, Map<String, Integer>>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedResult != null) {
            return cachedResult;
        }

        DateDim targetDate = dateDimRepository.findByYearAndMonthAndDay(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth()
        ).orElseThrow(() -> new DataNotFoundException("Invalid date: " + date));

        return getAndCacheDetailedProvinceStats(targetDate, cacheKey);
    }

    private Map<String, Map<String, Integer>> getAndCacheDetailedProvinceStats(DateDim date, String cacheKey) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();

        List<String> provinces = regionRepository.findAllDistinctProvinces();
        if (provinces.isEmpty()) {
            throw new DataNotFoundException("No province data found");
        }

        for (String province : provinces) {
            Map<String, Integer> provinceData = new LinkedHashMap<>();

            List<Integer> cityIds = regionRepository.findRegionIdsByProvince(province);
            if (cityIds.isEmpty()) {
                // 全部字段默认0
                provinceData.put("累计确诊人数", 0);
                provinceData.put("累计死亡人数", 0);
                provinceData.put("累计治愈人数", 0);
                provinceData.put("累计境外输入人数", 0);
                provinceData.put("新增确诊人数", 0);
                provinceData.put("新增死亡人数", 0);
                provinceData.put("新增治愈人数", 0);
                provinceData.put("新增疑似病例数", 0);
                result.put(province, provinceData);
                continue;
            }

            // 取累计数据（historical_stats）
            Integer totalConfirmed = historicalStatsRepository.findTotalConfirmedByDateAndRegions(date.getDateId(), cityIds).orElse(0);
            Integer totalDead = historicalStatsRepository.findTotalDeadByDateAndRegions(date.getDateId(), cityIds).orElse(0);
            Integer totalCured = historicalStatsRepository.findTotalCuredByDateAndRegions(date.getDateId(), cityIds).orElse(0);
            Integer totalImported = historicalStatsRepository.findTotalImportedByDateAndRegions(date.getDateId(), cityIds).orElse(0);

            // 取新增数据（daily_stats）
            Integer newConfirmed = dailyStatsRepository.findNewConfirmedByDateAndRegions(date.getDateId(), cityIds).orElse(0);
            Integer newDead = dailyStatsRepository.findNewDeadByDateAndRegions(date.getDateId(), cityIds).orElse(0);
            Integer newCured = dailyStatsRepository.findNewCuredByDateAndRegions(date.getDateId(), cityIds).orElse(0);
            Integer newSuspected = dailyStatsRepository.findNewSuspectedByDateAndRegions(date.getDateId(), cityIds).orElse(0);

            provinceData.put("累计确诊人数", totalConfirmed);
            provinceData.put("累计死亡人数", totalDead);
            provinceData.put("累计治愈人数", totalCured);
            provinceData.put("累计境外输入人数", totalImported);

            provinceData.put("新增确诊人数", newConfirmed);
            provinceData.put("新增死亡人数", newDead);
            provinceData.put("新增治愈人数", newCured);
            provinceData.put("新增疑似病例数", newSuspected);

            result.put(province, provinceData);
        }

        redisTemplate.opsForValue().set(cacheKey, result, CACHE_TTL);
        return result;
    }
}