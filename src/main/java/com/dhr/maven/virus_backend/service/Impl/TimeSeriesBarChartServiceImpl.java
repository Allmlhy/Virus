package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.repository.TimeSeriesBarChartRepository;
import com.dhr.maven.virus_backend.service.TimeSeriesBarChartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;



@Service
public class TimeSeriesBarChartServiceImpl implements TimeSeriesBarChartService {

        @Autowired
        private TimeSeriesBarChartRepository repo;

        @Override
        public List<Map<String, Object>> getDailyStatsWithRates(Integer year, Integer month, String province, String city) {
            List<Map<String, Object>> dailyStats = repo.findDailyStats(year, month, province, city);

            // 计算增长率v和加速度a
            for (int i = 0; i < dailyStats.size(); i++) {
                Map<String, Object> today = dailyStats.get(i);
                Integer newConfirmed = ((Number) today.get("newConfirmed")).intValue();

                double v = 0.0, a = 0.0;
                if (i > 0) {
                    Map<String, Object> yesterday = dailyStats.get(i - 1);
                    Integer prevConfirmed = ((Number) yesterday.get("newConfirmed")).intValue();
                    v = prevConfirmed == 0 ? 0 : (double)(newConfirmed - prevConfirmed) / prevConfirmed;
                }
                if (i > 1) {
                    Map<String, Object> yesterday = dailyStats.get(i - 1);
                    Map<String, Object> dayBeforeYesterday = dailyStats.get(i - 2);

                    double vYesterday = 0.0;
                    Integer newConfYesterday = ((Number) yesterday.get("newConfirmed")).intValue();
                    Integer newConfDayBefore = ((Number) dayBeforeYesterday.get("newConfirmed")).intValue();
                    if (newConfDayBefore != 0)
                        vYesterday = (double)(newConfYesterday - newConfDayBefore) / newConfDayBefore;

                    a = v - vYesterday;
                }

                today.put("growthRate", v);
                today.put("acceleration", a);
            }

            return dailyStats;
        }

    @Override
    public List<Map<String, Object>> getDailyStatsWholeChina(Integer year, Integer month) {
        List<Map<String, Object>> dailyStats = repo.findDailyStatsWholeChina(year, month);

        // 计算增长率v和加速度a
        for (int i = 0; i < dailyStats.size(); i++) {
            Map<String, Object> today = dailyStats.get(i);
            Integer newConfirmed = ((Number) today.get("newConfirmed")).intValue();

            double v = 0.0, a = 0.0;
            if (i > 0) {
                Map<String, Object> yesterday = dailyStats.get(i - 1);
                Integer prevConfirmed = ((Number) yesterday.get("newConfirmed")).intValue();
                v = prevConfirmed == 0 ? 0 : (double)(newConfirmed - prevConfirmed) / prevConfirmed;
            }
            if (i > 1) {
                Map<String, Object> yesterday = dailyStats.get(i - 1);
                Map<String, Object> dayBeforeYesterday = dailyStats.get(i - 2);

                double vYesterday = 0.0;
                Integer newConfYesterday = ((Number) yesterday.get("newConfirmed")).intValue();
                Integer newConfDayBefore = ((Number) dayBeforeYesterday.get("newConfirmed")).intValue();
                if (newConfDayBefore != 0)
                    vYesterday = (double)(newConfYesterday - newConfDayBefore) / newConfDayBefore;

                a = v - vYesterday;
            }

            today.put("growthRate", v);
            today.put("acceleration", a);
        }

        return dailyStats;
    }




}


