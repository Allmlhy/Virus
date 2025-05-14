package com.dhr.maven.virus_backend.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class CsvKafkaProducerApp2 {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC = "test2";

    public void sendCsv() throws Exception {
        String path = "src/main/java/com/dhr/maven/virus_backend/kafka/data/data2.csv";
        String csvData = Files.readAllLines(Paths.get(path))
                .stream().skip(1).reduce((l1, l2) -> l1 + "\n" + l2).orElse("");

        kafkaTemplate.send(TOPIC, csvData);
        System.out.println("App1 发送成功:\n" + csvData);
    }
}
