package com.dhr.maven.virus_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.*;

@Service
public class KafkaQueueService {

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(20000); // 最大缓存
    private final ExecutorService executor = Executors.newFixedThreadPool(2); // 多线程消费
    private final String topicName = "world-epidemic";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostConstruct
    public void startConsumer() {
        for (int i = 0; i < 2; i++) { // 可根据压力调整线程数量
            executor.submit(() -> {
                while (true) {
                    try {
                        String msg = messageQueue.take(); // 阻塞式获取
                        kafkaTemplate.send(topicName, msg); // 异步发送
                    } catch (Exception e) {
                        System.err.println("发送 Kafka 失败：" + e.getMessage());
                    }
                }
            });
        }
    }

    public void enqueue(String msg) throws InterruptedException {
        messageQueue.put(msg); // 阻塞式插入，保证顺序且不会内存溢出
    }
}
