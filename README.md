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

## 参考项目：https://github.com/CR553/Project01

![image](https://github.com/user-attachments/assets/1834fb61-1ac1-4610-bada-7cb588cf253d)

### 国内数据

<p float="left">
  <img src="https://github.com/user-attachments/assets/e888c593-88fd-4474-afe7-42a3dbecdd6e" width="45%" />
</p>
<p float="left">
  <img src="https://github.com/user-attachments/assets/69d8cafb-2273-4851-a419-14ec31e1f2cf" width="45%" />
</p>

### 导入历史数据
<img width="1280" alt="0b2d5ef33b29f27b67dddc5fa7bfcb9" src="https://github.com/user-attachments/assets/2961fa95-4ffb-4bc1-a3d0-6bb752595999" />
