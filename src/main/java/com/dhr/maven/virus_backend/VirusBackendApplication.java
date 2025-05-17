package com.dhr.maven.virus_backend;

import com.dhr.maven.virus_backend.kafka.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class VirusBackendApplication {
    @Autowired
    private CsvKafkaProducerApp1 producer1;

    @Autowired
    private CsvKafkaProducerApp3 producer3;

    @Autowired
    private CsvKafkaProducerApp4 producer4;

    @Autowired
    private CsvKafkaProducerApp5 producer5;

    @Autowired
    private CsvKafkaProducerApp6 producer6;

    @Autowired
    private CsvKafkaProducerApp7 producer7;
    public static void main(String[] args) {
        SpringApplication.run(VirusBackendApplication.class, args);
    }

    @PostConstruct
    public void startProducers() {
        new Thread(() -> {
            try {
                producer7.sendCsv();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();


    }
}

