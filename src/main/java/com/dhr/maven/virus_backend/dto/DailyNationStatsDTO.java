package com.dhr.maven.virus_backend.dto;


public class DailyNationStatsDTO {
    private Integer totalConfirmed;
    private Integer totalDeaths;
    private Integer totalRecovered;
    private Long dateId;

    // 构造函数必须与 JPQL 中的参数顺序和类型完全匹配
    public DailyNationStatsDTO(
            Integer totalConfirmed,
            Integer totalDeaths,
            Integer totalRecovered,
            Long dateId
    ) {
        this.totalConfirmed = totalConfirmed;
        this.totalDeaths = totalDeaths;
        this.totalRecovered = totalRecovered;
        this.dateId = dateId;
    }

    // Getters and Setters
    // ...

    public Integer getTotalConfirmed() {
        return totalConfirmed;
    }

    public void setTotalConfirmed(Integer totalConfirmed) {
        this.totalConfirmed = totalConfirmed;
    }
}