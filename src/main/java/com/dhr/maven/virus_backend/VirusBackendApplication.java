//package com.dhr.maven.virus_backend;
//
//import com.dhr.maven.virus_backend.kafka.CsvKafkaProducerApp1;
//import com.dhr.maven.virus_backend.kafka.CsvKafkaProducerApp2;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//import javax.annotation.PostConstruct;
//import java.util.Scanner;
//
//@SpringBootApplication
//public class VirusBackendApplication {
//
//    @Autowired
//    private CsvKafkaProducerApp1 producer1;
//
//    @Autowired
//    private CsvKafkaProducerApp2 producer2;
//
//    public static void main(String[] args) {
//        SpringApplication.run(VirusBackendApplication.class, args);
//    }
//
//    @PostConstruct
//    public void waitForUserInput() {
//        new Thread(() -> {
//            Scanner scanner = new Scanner(System.in);
//            while (true) {
//                System.out.print("输入 1 或 2 选择发送哪个 Kafka Producer：");
//                String input = scanner.nextLine();
//                try {
//                    if ("1".equals(input)) {
//                        producer1.sendCsv();
//                    } else if ("2".equals(input)) {
//                        producer2.sendCsv();
//                    } else {
//                        System.out.println("无效输入，请输入 1 或 2");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
//}


package com.dhr.maven.virus_backend;

import com.dhr.maven.virus_backend.kafka.CsvKafkaProducerApp1;
import com.dhr.maven.virus_backend.kafka.CsvKafkaProducerApp2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class VirusBackendApplication {

    @Autowired
    private CsvKafkaProducerApp1 producer1;

    @Autowired
    private CsvKafkaProducerApp2 producer2;

    public static void main(String[] args) {
        SpringApplication.run(VirusBackendApplication.class, args);
    }

    @PostConstruct
    public void startProducers() {
        // 启动 producer1 的线程
        new Thread(() -> {
            try {
                producer1.sendCsv();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 启动 producer2 的线程
        new Thread(() -> {
            try {
                producer2.sendCsv();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

