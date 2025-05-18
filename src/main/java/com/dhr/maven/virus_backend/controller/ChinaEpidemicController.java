package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.pojo.ChinaEpidemic;
import com.dhr.maven.virus_backend.service.ChinaEpidemicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/epidemic")
public class ChinaEpidemicController {

    @Autowired
    private ChinaEpidemicService service;

    // 简单GET接口，支持country, province, city三参数，均可不传，模糊过滤用等于
    @GetMapping("/search")
    public List<ChinaEpidemic> search(
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city) {

        return service.getByLocation(country, province, city);
    }
}
