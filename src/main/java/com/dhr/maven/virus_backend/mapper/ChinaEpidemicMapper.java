package com.dhr.maven.virus_backend.mapper;

import com.dhr.maven.virus_backend.pojo.ChinaEpidemic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChinaEpidemicMapper {

    @Select("<script>" +
            "SELECT * FROM china_epidemic WHERE 1=1 " +
            "<if test='country != null and country != \"\"'> AND country = #{country} </if>" +
            "<if test='province != null and province != \"\"'> AND province = #{province} </if>" +
            "<if test='city != null and city != \"\"'> AND city = #{city} </if>" +
            "</script>")
    List<ChinaEpidemic> findByLocation(@Param("country") String country,
                                       @Param("province") String province,
                                       @Param("city") String city);
}
