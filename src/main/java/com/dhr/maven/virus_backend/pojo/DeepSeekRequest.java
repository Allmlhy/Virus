package com.dhr.maven.virus_backend.pojo;

import lombok.Data;

import java.util.List;

@Data
public class DeepSeekRequest {
    private String model = "deepseek-chat";
    private List<Message> messages;
    private double temperature = 0.7;

    @Data
    public static class Message {
        private String role; // 如 "user"
        private String content;
    }
}
