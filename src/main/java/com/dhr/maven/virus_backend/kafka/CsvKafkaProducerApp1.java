package com.dhr.maven.virus_backend.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvKafkaProducerApp1 {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    //    private final String TOPIC = "china-covid-raw";
    private final String TOPIC = "china-covid-history-data-temp-temp";
    public void sendCsv() throws Exception {
        String csvPath = "src/main/java/com/dhr/maven/virus_backend/kafka/data/中国各省份数据_无疫苗_cleaned.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String header = br.readLine(); // 跳过表头
            String line;
            int batchSize = 100;
            int counter = 0;
            List<String> batch = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                batch.add(line);
                counter++;

                if (counter % batchSize == 0) {
                    sendBatch(batch);
                    batch.clear();
                    Thread.sleep(100); // 控制速率，避免阻塞
                }
            }

            if (!batch.isEmpty()) {
                sendBatch(batch);
            }

            System.out.println("App1 发送完成");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendBatch(List<String> batch) {
        for (String record : batch) {
            String[] fields = record.split(",");
            if (fields.length >= 2) {
                String province = fields[1];
                kafkaTemplate.send(TOPIC, province, record);
            }
        }
    }
}
