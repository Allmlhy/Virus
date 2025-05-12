import pymysql
import pandas as pd
from tqdm import tqdm

# MySQL 数据库连接配置
db_config = {
    'host': 'localhost',     # 数据库地址
    'user': 'root',          # 数据库用户名
    'password': 'root',      # 数据库密码
    'database': 'pro',       # 数据库名称
    'charset': 'utf8mb4'
}

# 读取 Excel 文件
file_path = '中国各省份数据_无疫苗_清洗填充.xlsx'  # <-- 修改为你的文件路径
df = pd.read_excel(file_path)

# 规范列名
df.columns = df.columns.str.strip()

# 转换需要汇总的列为数值类型，非数值部分使用 NaN
numeric_columns = ['确诊', '新增确诊', '疑似', '新增治愈', '治愈', '死亡', '新增死亡']
for column in numeric_columns:
    df[column] = pd.to_numeric(df[column], errors='coerce')

# 创建数据库连接
connection = pymysql.connect(**db_config)
cursor = connection.cursor()

# # 插入数据到 details 表
# insert_sql_details = """
#     INSERT INTO details (update_time, province, city, confirm, confirm_add, heal, dead)
#     VALUES (%s, %s, %s, %s, %s, %s, %s)
# """

# insert_values_details = []

# for _, row in tqdm(df.iterrows(), total=df.shape[0], desc="Preparing data for details table", ncols=100):
#     update_time = row['日期'].strftime('%Y-%m-%d %H:%M:%S') if not pd.isnull(row['日期']) else ''
#     province = row['省']
#     city = row['市']
#     confirm = int(row['确诊']) if not pd.isnull(row['确诊']) else 0
#     confirm_add = int(row['新增确诊']) if not pd.isnull(row['新增确诊']) else 0
#     heal = int(row['治愈']) if not pd.isnull(row['治愈']) else 0
#     dead = int(row['死亡']) if not pd.isnull(row['死亡']) else 0

#     insert_values_details.append((update_time, province, city, confirm, confirm_add, heal, dead))

# # 批量插入数据到 details 表
# cursor.executemany(insert_sql_details, insert_values_details)
# connection.commit()

# 插入数据到 history 表（按日期聚合）
history_df = df.groupby('日期').agg({
    '确诊': 'sum',
    '新增确诊': 'sum',
    '疑似': 'sum',
    '新增治愈': 'sum',
    '治愈': 'sum',
    '死亡': 'sum',
    '新增死亡': 'sum'
}).reset_index()

# 将所有NaN值填充为0
history_df = history_df.fillna(0)

insert_sql_history = """
    INSERT INTO history (ds, confirm, confirm_add, suspect, suspect_add, heal, heal_add, dead, dead_add)
    VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
"""

insert_values_history = []

for _, row in tqdm(history_df.iterrows(), total=history_df.shape[0], desc="Preparing data for history table", ncols=100):
    ds = row['日期'].strftime('%Y-%m-%d')
    confirm = int(row['确诊'])
    confirm_add = int(row['新增确诊'])
    suspect = int(row['疑似'])
    suspect_add = 0  # 源数据中无“新增疑似”，置为0
    heal = int(row['治愈'])
    heal_add = int(row['新增治愈']) if not pd.isnull(row['新增治愈']) else 0
    dead = int(row['死亡'])
    dead_add = int(row['新增死亡']) if not pd.isnull(row['新增死亡']) else 0

    insert_values_history.append((ds, confirm, confirm_add, suspect, suspect_add, heal, heal_add, dead, dead_add))

# 批量插入数据到 history 表
cursor.executemany(insert_sql_history, insert_values_history)
connection.commit()

# 关闭数据库连接
cursor.close()
connection.close()

print("数据已成功插入数据库！")
