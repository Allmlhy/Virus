import pymysql
import pandas as pd

db_config = {
    'host': '106.12.170.52',
    'user': 'lmx',
    'password': 'lmx',
    'database': 'sparkprogram',
    'charset': 'utf8mb4',
    'port': 13327,   # 注意这里是整数
    'cursorclass': pymysql.cursors.DictCursor
}


# 读取CSV文件路径
csv_file = 'your_data.csv'  # 替换为你的文件路径

def load_countries_to_db(csv_path):
    df = pd.read_csv(csv_path, encoding='utf-8-sig')
    df.columns = df.columns.str.strip()
    
    insert_data = []
    for idx, row in df.iterrows():
        country_code = str(row.get('国家代码', '')).strip()
        if country_code in ('nan', ''):
            country_code = None  # 或者你想用别的默认值，比如 'UNKNOWN'
        
        country_name = str(row.get('国家', '')).strip()
        region_code = str(row.get('世界卫生组织地区', '')).strip() if pd.notna(row.get('世界卫生组织地区')) else ''
        who_region = None
        level = 0
        
        if country_code is None or country_name == '':
            # 跳过关键字段缺失的行
            continue
        
        insert_data.append((country_code, country_name, region_code, who_region, level))
    
    # 数据库连接与插入逻辑不变
    import pymysql
    conn = pymysql.connect(**db_config)
    try:
        with conn.cursor() as cursor:
            sql = """
            INSERT INTO countries (country_code, country_name, region_code, who_region, level)
            VALUES (%s, %s, %s, %s, %s)
            ON DUPLICATE KEY UPDATE
                country_name=VALUES(country_name),
                region_code=VALUES(region_code),
                who_region=VALUES(who_region),
                level=VALUES(level)
            """
            cursor.executemany(sql, insert_data)
            conn.commit()
            print(f"成功插入或更新了{cursor.rowcount}条记录")
    finally:
        conn.close()



if __name__ == "__main__":
    load_countries_to_db(r"F:\实时大数据处理技术\课设\世界各国数据_无疫苗_cleaned(1).csv")

    
