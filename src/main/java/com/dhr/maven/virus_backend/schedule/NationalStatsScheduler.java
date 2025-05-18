package com.dhr.maven.virus_backend.schedule;

import com.dhr.maven.virus_backend.service.NationalStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NationalStatsScheduler {

    @Autowired
    private NationalStatsService nationalStatsService;

    /**
     * 每天凌晨1点定时更新全国统计数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleUpdate() {
        nationalStatsService.updateNationalStats();
        System.out.println("定时任务已执行：更新全国统计数据");
    }
}
