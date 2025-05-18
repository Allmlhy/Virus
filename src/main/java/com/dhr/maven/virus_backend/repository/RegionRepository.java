package com.dhr.maven.virus_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dhr.maven.virus_backend.pojo.Region;
import java.util.List;

// RegionRepository.java
public interface RegionRepository extends JpaRepository<Region, Integer> {

    List<Region> findByLevel(int level);
    // ✅ 新增：查询所有不重复的省份名
    @Query("SELECT DISTINCT r.province FROM Region r")
    List<String> findAllDistinctProvinces();

    @Query("SELECT r.regionId FROM Region r WHERE r.province = ?1")
    List<Integer> findRegionIdsByProvince(String province);
}