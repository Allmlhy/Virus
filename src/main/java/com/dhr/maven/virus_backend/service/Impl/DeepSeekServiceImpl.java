package com.dhr.maven.virus_backend.service.Impl;

import com.dhr.maven.virus_backend.config.AppConfig;
import com.dhr.maven.virus_backend.pojo.DeepSeekRequest;
import com.dhr.maven.virus_backend.pojo.DeepSeekResponse;
import com.dhr.maven.virus_backend.service.DeepSeekService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class DeepSeekServiceImpl implements DeepSeekService {

    private final AppConfig config;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String ask(String question) {
        // 构造请求体
        DeepSeekRequest request = new DeepSeekRequest();
        DeepSeekRequest.Message message = new DeepSeekRequest.Message();
        message.setRole("user");
        message.setContent(question);
        request.setMessages(Collections.singletonList(message));

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());

        HttpEntity<DeepSeekRequest> entity = new HttpEntity<>(request, headers);

        // 发起请求
        ResponseEntity<DeepSeekResponse> response = restTemplate.postForEntity(
                config.getApiUrl(),
                entity,
                DeepSeekResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody()
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
        } else {
            return "调用 DeepSeek 接口失败";
        }
    }
}
