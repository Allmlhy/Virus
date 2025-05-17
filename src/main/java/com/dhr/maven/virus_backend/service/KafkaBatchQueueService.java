<<<<<<< HEAD
package com.dhr.maven.virus_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class KafkaBatchQueueService {

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(20000); // 缓冲队列
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 定时调度
    private final ExecutorService senderExecutor = Executors.newSingleThreadExecutor();     // 发送线程

    private final int BATCH_SIZE = 100;     // 每批最多条数
    private final int BATCH_INTERVAL_MS = 5000; // 每批最长间隔（毫秒）

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName = "world-epidemic";

    @PostConstruct
    public void startBatchSender() {
        // 定时调度：每 BATCH_INTERVAL_MS 检查队列是否有数据
        scheduler.scheduleAtFixedRate(this::sendBatchIfDue, BATCH_INTERVAL_MS, BATCH_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // 持续监听是否达到 BATCH_SIZE，立即发送
        senderExecutor.submit(() -> {
            List<String> batch = new ArrayList<>();
            while (true) {
                try {
                    String msg = messageQueue.take();
                    batch.add(msg);
                    if (batch.size() >= BATCH_SIZE) {
                        sendBatch(new ArrayList<>(batch));
                        batch.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 手动触发：超时后发送剩余批次
    private void sendBatchIfDue() {
        List<String> batch = new ArrayList<>();
        messageQueue.drainTo(batch, BATCH_SIZE);
        if (!batch.isEmpty()) {
            sendBatch(batch);
        }
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
}
=======
package com.dhr.maven.virus_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class KafkaBatchQueueService {

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>(20000); // 缓冲队列
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 定时调度
    private final ExecutorService senderExecutor = Executors.newSingleThreadExecutor();     // 发送线程

    private final int BATCH_SIZE = 100;     // 每批最多条数
    private final int BATCH_INTERVAL_MS = 5000; // 每批最长间隔（毫秒）

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String topicName = "world-epidemic";

    @PostConstruct
    public void startBatchSender() {
        // 定时调度：每 BATCH_INTERVAL_MS 检查队列是否有数据
        scheduler.scheduleAtFixedRate(this::sendBatchIfDue, BATCH_INTERVAL_MS, BATCH_INTERVAL_MS, TimeUnit.MILLISECONDS);

        // 持续监听是否达到 BATCH_SIZE，立即发送
        senderExecutor.submit(() -> {
            List<String> batch = new ArrayList<>();
            while (true) {
                try {
                    String msg = messageQueue.take();
                    batch.add(msg);
                    if (batch.size() >= BATCH_SIZE) {
                        sendBatch(new ArrayList<>(batch));
                        batch.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // 手动触发：超时后发送剩余批次
    private void sendBatchIfDue() {
        List<String> batch = new ArrayList<>();
        messageQueue.drainTo(batch, BATCH_SIZE);
        if (!batch.isEmpty()) {
            sendBatch(batch);
        }
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
}
>>>>>>> 09081cd (更新)
