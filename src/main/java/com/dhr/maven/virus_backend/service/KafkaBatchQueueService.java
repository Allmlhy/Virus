package com.dhr.maven.virus_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class KafkaBatchQueueService {

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(20000); // 缓冲队列
    private final ExecutorService senderExecutor = Executors.newFixedThreadPool(4); // 发送线程

    private final int BATCH_SIZE = 1000;        // 每批最多条数
    private final int BATCH_INTERVAL_MS = 1000; // 每批最长间隔（毫秒）

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName = "world-epidemic";

    @PostConstruct
    public void startBatchSender() {
        senderExecutor.submit(() -> {
            List<String> batch = new ArrayList<>();
            long lastSendTime = System.currentTimeMillis();

            while (true) {
                try {
                    String msg = messageQueue.poll(100, TimeUnit.MILLISECONDS); // 非阻塞获取

                    if (msg != null) {
                        batch.add(msg);
                    }

                    long now = System.currentTimeMillis();
                    boolean timeExceeded = (now - lastSendTime >= BATCH_INTERVAL_MS);

                    if ((batch.size() >= BATCH_SIZE) || (timeExceeded && !batch.isEmpty())) {
                        sendBatch(new ArrayList<>(batch));
                        batch.clear();
                        lastSendTime = now;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendBatch(List<String> batch) {
        for (String msg : batch) {
            kafkaTemplate.send(topicName, msg);
        }
        System.out.println("批量发送 " + batch.size() + " 条数据到 Kafka");
    }

    public void enqueue(String msg) throws InterruptedException {
        messageQueue.put(msg);
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("服务即将关闭，尝试发送剩余消息...");
        List<String> remaining = new ArrayList<>();
        messageQueue.drainTo(remaining);
        if (!remaining.isEmpty()) {
            sendBatch(remaining);
        }
        senderExecutor.shutdown();
    }
}
