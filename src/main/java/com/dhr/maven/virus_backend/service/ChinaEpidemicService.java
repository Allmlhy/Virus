package com.dhr.maven.virus_backend.service;

import com.dhr.maven.virus_backend.mapper.ChinaEpidemicMapper;
import com.dhr.maven.virus_backend.pojo.ChinaEpidemic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChinaEpidemicService {

    @Autowired
    private ChinaEpidemicMapper mapper;

    public List<ChinaEpidemic> getByLocation(String country, String province, String city) {
        return mapper.findByLocation(country, province, city);
    }
}
