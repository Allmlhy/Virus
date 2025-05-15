import pandas as pd
import pymysql
from datetime import datetime

# 1. 读取 CSV 文件
csv_path = r'F:\实时大数据处理技术\Virus-backend\src\main\java\com\dhr\maven\virus_backend\kafka\data\中国各省份数据_无疫苗_cleaned.csv'
df = pd.read_csv(csv_path, encoding='utf-8', sep=',')

# 2. 提取唯一日期并转换为 year, month, day
def parse_date_info(date_str):
    try:
        dt = datetime.strptime(date_str.strip(), '%Y-%m-%d')  # 根据你的日期格式调整
        return dt.year, dt.month, dt.day
    except Exception as e:
        print(f"解析日期失败: {date_str} -> {e}")
        return None

unique_dates = df['日期'].dropna().unique()

date_parsed_list = []
for date_str in unique_dates:
    parsed = parse_date_info(date_str)
    if parsed:
        date_parsed_list.append(parsed)

# 3. 给日期排序，赋予自增 date_id（1开始）
date_parsed_list.sort()  # 按年、月、日排序

date_data = []
for idx, (year, month, day) in enumerate(date_parsed_list, start=1):
    date_data.append((idx, year, month, day))

# 4. 存入 MySQL 的 date_dim 表
connection = pymysql.connect(
    host='106.12.170.52',
    user='lmx',
    password='lmx',
    database='sparkprogram',
    charset='utf8mb4',
    port=13327
)

insert_sql = '''
INSERT INTO date_dim (date_id, year, month, day)
VALUES (%s, %s, %s, %s)
ON DUPLICATE KEY UPDATE
year=VALUES(year), month=VALUES(month), day=VALUES(day)
'''

try:
    with connection.cursor() as cursor:
        cursor.executemany(insert_sql, date_data)
    connection.commit()
    print(f"成功插入 {len(date_data)} 条日期记录。")
except Exception as e:
    print("插入数据库失败：", e)
finally:
    connection.close()
