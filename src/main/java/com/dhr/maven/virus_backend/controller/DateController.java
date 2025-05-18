package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.DateService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/date")
@CrossOrigin
public class DateController {

    @Resource
    private DateService dateService;

    @GetMapping("/structure")
    public String getDateStructure() {
        return dateService.getDateStructureJson();
    }
}
