package com.dhr.maven.virus_backend.dao;


import org.springframework.stereotype.Repository;

@Repository
public class DeepSeekDAO {

    // 模拟保存预测结果到数据库（实际操作根据你的数据库来定义）
    public void savePrediction(String prediction, double confidence) {
        // 假设我们将结果保存到数据库
        System.out.println("Saving prediction: " + prediction + " with confidence: " + confidence);
        // 这里你可以添加真正的数据库操作代码
    }
}
