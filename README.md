# Virus
# 🌍 全球新冠疫情实时分析与可视化系统

基于大数据技术栈（Spark、Kafka、MySQL、Redis、Java、Python）构建的全球新冠疫情实时数据分析与可视化系统，实现从数据采集到实时处理、存储及前端动态展示的完整流程，适用于疫情监控、数据分析和决策支持。

---

## 📌 项目功能

- 实时采集全球各地疫情数据（国家、省份级）
- Kafka 分布式消息传输
- Spark Streaming 实时处理与数据清洗
- MySQL 存储历史记录，Redis 提供快速缓存查询
- 基于 ECharts 的前端疫情可视化展示

---

## 🧱 技术栈

| 层级 | 技术 |
|------|------|
| 数据采集 | Python（爬虫 / API） |
| 消息队列 | Kafka |
| 实时计算 | Spark Streaming |
| 数据存储 | MySQL、Redis |
| 后端服务 | Java（Spring Boot） |
| 数据可视化 | HTML、JavaScript、ECharts |

---

## 📂 项目结构

```bash
covid19-visual-system/
├── data-crawler/        # Python数据采集脚本
├── kafka-producer/      # Kafka消息生产模块
├── spark-streaming/     # Spark实时数据处理
├── backend-api/         # Java后端API服务
├── dashboard-front/     # 前端展示页面（ECharts）
├── sql/                 # 初始化数据库脚本
└── README.md            # 项目说明文件
```

## 🏗️ 系统架构图

> 请将下方图片链接替换为你的实际架构图

![系统架构图](https://your-image-link.com/architecture.png)

---

## 🖥️ 可视化效果展示

> 请将下方图片链接替换为你的前端页面截图

![可视化大屏](https://your-image-link.com/visual-dashboard.png)

---
## 🔧 本地运行指南

### 1️⃣ 启动 Kafka 和 Zookeeper

#### 启动 Zookeeper
bin/zookeeper-server-start.sh config/zookeeper.properties

#### 启动 Kafka
bin/kafka-server-start.sh config/server.properties

### 2️⃣ 创建 Kafka Topic

bin/kafka-topics.sh --create --topic covid-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

### 3️⃣ 启动数据采集模块

cd data-crawler
python run.py

### 4️⃣ 启动 Spark Streaming 实时计算模块

cd spark-streaming
spark-submit --master local[*] main.py

### 5️⃣ 启动后端 API 服务

cd backend-api
mvn spring-boot:run

### 6️⃣ 启动前端页面

# 可直接通过浏览器打开前端页面
open dashboard-front/index.html

---

### 🧾 数据来源

- 丁香园疫情数据
- 约翰霍普金斯大学 COVID-19 数据仓库

---
### 📜 许可证 License

本项目采用 MIT License 许可。


---

## 参考项目：https://github.com/CR553/Project01

### 导入历史数据
<img width="1280" alt="0b2d5ef33b29f27b67dddc5fa7bfcb9" src="https://github.com/user-attachments/assets/2961fa95-4ffb-4bc1-a3d0-6bb752595999" />

### 国内数据建表

<p float="left">
  <img src="https://github.com/user-attachments/assets/e888c593-88fd-4474-afe7-42a3dbecdd6e" width="45%" />
</p>
<p float="left">
  <img src="https://github.com/user-attachments/assets/69d8cafb-2273-4851-a419-14ec31e1f2cf" width="45%" />
</p>


