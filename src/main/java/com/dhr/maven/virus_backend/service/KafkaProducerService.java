package com.dhr.maven.virus_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName = "world-epidemic";

    public void sendMessage(String message) {
        kafkaTemplate.send(topicName, message);
    }
}