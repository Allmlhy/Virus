package com.dhr.maven.virus_backend;


import com.dhr.maven.virus_backend.kafka.CovidKafkaProducer;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableJpaRepositories("com.dhr.maven.virus_backend.repository")
@MapperScan("com.dhr.maven.virus_backend.mapper")  // 指定Mapper接口所在包路径
public class VirusBackendApplication {
    @Autowired
    private CovidKafkaProducer producer1;
    @Autowired

    public static void main(String[] args) {
        SpringApplication.run(VirusBackendApplication.class, args);
    }

}


