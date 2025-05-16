## 新增数据的数据库后台系统

### 项目依赖

```
pip install flask pymysql
```

### 项目运行

运行`app.py`文件即可.

### 项目效果展示

首页欢迎页.

![hello](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\hello.jpg)

世界疫情数据播报页，可以依次选择国家、日期进行查看，查看到的信息包括：

- 当日新增确诊病例
- 累计确诊病例
- 当日新增死亡病例
- 累计死亡病例

如`中国-2025-5-16`

![global](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\global.jpg)

![global_date](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\global_date.jpg)

![global_date_country_stats](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\global_date_country_stats.jpg)

中国疫情数据播报页，可以依次选择省份、日期进行查看，查看到的信息包括：

- 当日该省-市的新增确诊病例
- 当日该省-市的新增死亡病例
- 当日该省-市的新增治愈病例
- 当日该省-市的新增疑似病例

如`云南-2021-12-25`

![china](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\china.jpg)

![china_date](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\china_date.jpg)

中国疫情数据录入页.

依次输入报告日期、省份、城市（地级市）、新增确诊、新增死亡、新增治愈和疑似病例，同一省份\城市\日期不能够重复填写.

![add_china](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\add_china.jpg)

世界疫情数据录入页.

依次输入报告日期、国家名、新增确诊、累计确诊、新增死亡和累计死亡，同一国家\日期不能够重复填写.

![add_country](D:\BJFU\2024-2025-2\Course-实时大数据处理技术\courseDesgin-dev\flaskProject\pic\add_country.jpg)