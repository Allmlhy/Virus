package com.dhr.maven.virus_backend.repository;

import com.dhr.maven.virus_backend.pojo.DateDim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DateDimRepository extends JpaRepository<DateDim, Integer> {

    // 获取 dateId 最大的一条记录
    Optional<DateDim> findTopByOrderByDateIdDesc();


    // 根据年份获取 dateId 最大的一条记录
    @Query("SELECT d FROM DateDim d WHERE d.year = ?1 ORDER BY d.dateId DESC")
    Optional<DateDim> findTopByYearOrderByDateIdDesc(int year);

    // 根据年月日查询对应记录
    Optional<DateDim> findByYearAndMonthAndDay(int year, int month, int day);
}
