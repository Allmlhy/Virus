package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class DeepSeekController {

    private final DeepSeekService deepSeekService;

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return deepSeekService.ask(question);
    }
}
