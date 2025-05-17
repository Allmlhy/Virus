package com.dhr.maven.virus_backend.kafka;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class CsvKafkaProducerApp7 {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC = "Country_Vaccinations";

    public void sendCsv() throws Exception {
        String path = "src/main/java/com/dhr/maven/virus_backend/kafka/data/世界疫苗接种数据_cleaned.csv";
        List<String> lines = Files.readAllLines(Paths.get(path));

        int total = lines.size() - 1;
        int batchSize = 100;        // 每批发送200条
        int delayMs = 500;          // 每批发送后暂停500毫秒
        int successCount = 0;

        System.out.println("开始发送，共计数据行数: " + total);

        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);

            boolean sent = false;
            int retry = 0;

            while (!sent && retry < 3) {
                try {
                    // KafkaTemplate 的 send().get() 返回 SendResult
                    SendResult<String, String> result = kafkaTemplate.send(TOPIC, line).get();
                    RecordMetadata metadata = result.getRecordMetadata();

                    System.out.println("发送成功: 第 " + i + " 行, partition=" + metadata.partition() + ", offset=" + metadata.offset());
                    sent = true;
                    successCount++;

                } catch (ExecutionException | InterruptedException e) {
                    retry++;
                    System.err.println("发送失败，正在重试(" + retry + ")：第 " + i + " 行，错误: " + e.getMessage());
                    Thread.sleep(200); // 重试前等待200ms
                }
            }

            if (i % batchSize == 0) {
                System.out.println("已发送: " + successCount + " / " + total);
                Thread.sleep(delayMs);  // 控制速率
            }
        }

        System.out.println("发送完成，总共成功发送行数: " + successCount);
    }
}
