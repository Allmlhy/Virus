package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.pojo.DateDim;
import com.dhr.maven.virus_backend.repository.DateDimRepository;
import com.dhr.maven.virus_backend.service.DateService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.dhr.maven.virus_backend.config.RedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DateServiceImpl implements DateService {

    @Autowired
    private DateDimRepository dateDimRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY = "date_structure";

    @Override
    public String getDateStructureJson() {
        // 1. Redis 缓存查询
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        if (cached != null) {
            return cached;
        }

        // 2. MySQL 查询
        List<DateDim> dates = dateDimRepository.findAll();

        // 3. 构建结构 year -> month -> days[]
        Map<Integer, Map<Integer, Set<Integer>>> map = new TreeMap<>();
        for (DateDim date : dates) {
            int year = date.getYear();
            int month = date.getMonth();
            int day = date.getDay();

            map.computeIfAbsent(year, y -> new TreeMap<>())
                    .computeIfAbsent(month, m -> new TreeSet<>())
                    .add(day);
        }

        // 4. 转换 Set 为 List 以序列化
        Map<Integer, Map<Integer, List<Integer>>> result = new TreeMap<>();
        for (Map.Entry<Integer, Map<Integer, Set<Integer>>> entry : map.entrySet()) {
            Map<Integer, List<Integer>> monthMap = new TreeMap<>();
            for (Map.Entry<Integer, Set<Integer>> sub : entry.getValue().entrySet()) {
                monthMap.put(sub.getKey(), new ArrayList<>(sub.getValue()));
            }
            result.put(entry.getKey(), monthMap);
        }

        try {
            // 5. 序列化为 JSON 并存入 Redis
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(REDIS_KEY, json);
            return json;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }
    }
}
