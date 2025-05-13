import pandas as pd

# 1. 读取 Excel 表格
file_path = "../data/中国各省份数据_无疫苗.xlsx"
df = pd.read_excel(file_path)

# 1. 删除整列都是空值的列
df.dropna(axis=1, how='all', inplace=True)

# 2. 如果“市”列为空，则将其填充为“省”列的值
df['市'] = df['市'].fillna(df['省'])

# 3. 将“日期”列转换为标准 datetime 类型，再格式化为“2020-1-20”样式
df['日期'] = pd.to_datetime(df['日期'], errors='coerce')
df['日期'] = df['日期'].dt.date  # 只保留日期部分

# 4. 将“新增治愈”列中小于 0 的值替换为 0
if '新增治愈' in df.columns:
    df['新增治愈'] = df['新增治愈'].apply(lambda x: max(x, 0) if pd.notnull(x) else x)

# 5. 填充指定列中的空值为 0
columns_to_fill = ['确诊', '新增确诊', '现存确诊', '死亡', '新增死亡', '死亡率', '治愈', '新增治愈', '治愈率','疑似','累积境外输入','无症状感染']
for col in columns_to_fill:
    if col in df.columns:
        df[col] = df[col].fillna(0)

# 输出看看结果
print(df.head())

# 保存处理结果
df.to_excel("../data/中国各省份数据_无疫苗_cleaned.xlsx", index=False)
