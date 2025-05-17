<<<<<<< HEAD
package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.KafkaBatchQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private KafkaBatchQueueService kafkaBatchQueueService;

    @PostMapping("/csv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("文件为空");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            boolean headerSkipped = false;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                kafkaBatchQueueService.enqueue(line);
                count++;
            }

            return ResponseEntity.ok("上传成功，共 " + count + " 行，正在批量发送 Kafka");
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(500).body("处理失败：" + e.getMessage());
        }
    }

}
=======
package com.dhr.maven.virus_backend.controller;

import com.dhr.maven.virus_backend.service.KafkaBatchQueueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private KafkaBatchQueueService kafkaBatchQueueService;

    @PostMapping("/csv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("文件为空");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            boolean headerSkipped = false;
            int count = 0;

            while ((line = reader.readLine()) != null) {
                if (!headerSkipped) {
                    headerSkipped = true;
                    continue;
                }
                kafkaBatchQueueService.enqueue(line);
                count++;
            }

            return ResponseEntity.ok("上传成功，共 " + count + " 行，正在批量发送 Kafka");
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(500).body("处理失败：" + e.getMessage());
        }
    }

}
>>>>>>> 09081cd (更新)
