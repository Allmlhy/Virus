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
![front](https://github.com/user-attachments/assets/f2f4309c-c294-4943-8b88-ea8be0224360)



---

## 🖥️ 可视化效果展示

> 请将下方图片链接替换为你的前端页面截图

<img width="1280" alt="63769b3b17517270c810f8b1ec6c060" src="https://github.com/user-attachments/assets/c52a1a0a-189c-4891-99c3-800e41e2149f" />



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

---
### 国内数据建表

# 国内数据建表

## ✅ 表1：regions（地区信息表）

| 字段名    | 中文含义     | 说明                           |
| --------- | ------------ | ------------------------------ |
| region_id | 地区编号     | 主键，唯一标识一个地区         |
| province  | 省份         | 地区所属的省级行政单位         |
| city      | 城市         | 地区所属的地级行政单位         |
| level     | 疫情严重等级 | 值为0-5，表示地区疫情严重等级 |

---

## ✅ 表2：date_dim（日期维度表）

| 字段名 | 中文含义 | 说明     |
| ------ | -------- | -------- |
| date_id | 日期ID   | 主键     |
| year   | 年       | 年份字段 |
| month  | 月       | 月份字段 |
| day    | 日       | 日期字段 |

---

## ✅ 表3：daily_stats（每日新增统计数据表）

| 字段名        | 中文含义       | 说明                                             |
| ------------- | -------------- | ------------------------------------------------ |
| id            | 主键编号       | 自增主键                                         |
| region_id     | 地区编号       | 外键，关联 regions(region_id)                  |
| date_id       | 日期           | 外键，关联 date_dim(date_id)                   |
| new_deaths    | 新增死亡人数   | 当天新增死亡病例                                 |
| new_confirmed | 新增确诊人数   | 当天新增确诊病例                                 |
| new_recovered | 新增治愈人数   | 当天新增治愈病例                                 |
| new_suspected | 新增疑似病例数 | 当天新增疑似病例数                               |
| v             | 每日增长率     | 新增确诊人数相较前一天的增长率                   |
| a             | 加速度         | 表示增长率的变化趋势（当天增长率 - 前一天）     |

---

## ✅ 表4：historical_stats（累计历史统计数据表）

| 字段名             | 中文含义             | 说明                                           |
| ------------------ | -------------------- | ---------------------------------------------- |
| id                 | 主键编号             | 自增主键                                       |
| region_id          | 地区编号             | 外键，关联 regions(region_id)                |
| date_id            | 日期                 | 外键，关联 date_dim(date_id)                 |
| total_confirmed    | 累计确诊人数         | 截至该日的累计确诊人数                         |
| total_deaths       | 累计死亡人数         | 截至该日的累计死亡人数                         |
| total_recovered    | 累计治愈人数         | 截至该日的累计治愈人数                         |
| total_imported     | 累计境外输入人数     | 累计的境外输入病例数                           |
| total_asymptomatic | 累计无症状感染者     | 截至该日的累计无症状感染者人数                 |
| current_confirmed  | 现存确诊人数         | 当前仍在治疗中的确诊人数（确诊-死亡-治愈）     |

# 国际数据建表

## ✅ 1. countries（国家信息表）

| 字段名       | 中文含义     | 说明                                           |
| ------------ | ------------ | ---------------------------------------------- |
| country_code | 国家代码     | 主键，唯一标识一个国家                         |
| country_name | 国家名称     | 国家中文或英文名称                            |
| region_code  | 地区代码     | 外键，关联 global_regions(region_code)        |
| who_region   | 世卫组织地区 | 世界卫生组织划定的区域                        |
| level        | 疫情严重等级 | 值为0-5，表示国家的疫情严重等级               |

---

## ✅ 2. global_regions（国际地区信息表）

| 字段名     | 中文含义 | 说明                         |
| ---------- | -------- | ---------------------------- |
| region_code| 地区代码 | 主键，唯一标识一个国际地区   |
| region_name| 地区名称 | 地区名称                     |

---

## ✅ 3. global_daily_stats（全球每日新增统计数据）

| 字段名        | 中文含义       | 说明                                             |
| ------------- | -------------- | ------------------------------------------------ |
| id            | 主键编号       | 自增主键                                         |
| country_code  | 国家代码       | 外键，关联 countries(country_code)             |
| date_id       | 日期           | 外键，关联 date_dim(date_id)                   |
| new_deaths    | 新增死亡人数   | 当天新增死亡病例                                 |
| new_confirmed | 新增确诊人数   | 当天新增确诊病例                                 |
| v             | 每日增长率     | 新增确诊人数相较前一天的增长率                   |
| a             | 加速度         | 表示增长率的变化趋势（当天增长率 - 前一天）     |

---

## ✅ 4. global_historical_stats（全球历史统计数据表）

| 字段名          | 中文含义       | 说明                                       |
| --------------- | -------------- | ------------------------------------------ |
| id              | 主键编号       | 自增主键                                   |
| country_code    | 国家代码       | 外键，关联 countries(country_code)        |
| date_id         | 日期           | 外键，关联 date_dim(date_id)             |
| total_confirmed | 累计确诊人数   | 截至该日的累计确诊人数                     |
| total_deaths    | 累计死亡人数   | 截至该日的累计死亡人数                     |

---

## ✅ 5. country_vaccinations（国家疫苗接种数据表）

| 字段名           | 中文含义         | 说明                                           |
| ---------------- | ---------------- | ---------------------------------------------- |
| id               | 主键编号         | 自增主键                                       |
| country_code     | 国家代码         | 外键，关联 countries(country_code)           |
| date_id          | 日期             | 外键，关联 date_dim(date_id)                 |
| total_doses      | 累计接种剂次     | 累计接种的疫苗剂次                             |
| vaccinated       | 接种过疫苗人数   | 至少接种过一次疫苗的人数                       |
| fully_vaccinated | 完全接种人数     | 完成规定剂次接种的人数                         |
| daily_doses      | 每日接种剂次     | 每日接种的疫苗剂次                             |
| daily_vaccinated | 每日新增接种人数 | 每日新接种疫苗人数                             |

---

## ✅ 6. region_vaccinations（地区疫苗接种数据表）

| 字段名           | 中文含义         | 说明                                           |
| ---------------- | ---------------- | ---------------------------------------------- |
| id               | 主键编号         | 自增主键                                       |
| region_code      | 地区代码         | 外键，关联 global_regions(region_code)       |
| date_id          | 日期             | 外键，关联 date_dim(date_id)                 |
| total_doses      | 累计接种剂次     | 累计接种的疫苗剂次                             |
| vaccinated       | 接种过疫苗人数   | 至少接种过一次疫苗的人数                       |
| fully_vaccinated | 完全接种人数     | 完成规定剂次接种的人数                         |
| daily_doses      | 每日接种剂次     | 每日接种的疫苗剂次                             |
| daily_vaccinated | 每日新增接种人数 | 每日新接种疫苗人数                             |
