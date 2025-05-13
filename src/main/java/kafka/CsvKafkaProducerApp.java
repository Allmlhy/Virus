package kafka;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class CsvKafkaProducerApp implements CommandLineRunner {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC = "test";

    public static void main(String[] args) {
        SpringApplication.run(CsvKafkaProducerApp.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String path = "src/main/java/kafka/data/data.csv"; // CSV 文件路径

        // 读取CSV文件为字符串（忽略表头）
        String csvData = Files.readAllLines(Paths.get(path))
                .stream()
                .skip(1) // 跳过表头
                .reduce((l1, l2) -> l1 + "\n" + l2)
                .orElse("");

        kafkaTemplate.send(TOPIC, csvData);
        System.out.println("CSV 数据已发送到 Kafka: \n" + csvData);
    }
}
