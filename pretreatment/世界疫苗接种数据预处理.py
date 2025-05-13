import pandas as pd

# 1. 读取 Excel 表格
file_path = "../data/世界疫苗接种数据.xlsx"
df = pd.read_excel(file_path)

# 1. 删除整列都是空值的列
df.dropna(axis=1, how='all', inplace=True)

# 3. 将“日期”列转换为标准 datetime 类型，再格式化为“2020-1-20”样式
df['日期'] = pd.to_datetime(df['日期'], errors='coerce')
df['日期'] = df['日期'].dt.date  # 只保留日期部分

# 5. 填充指定列中的空值为 0
columns_to_fill = [
    "累计接种剂次",
    "接种过疫苗人数",
    "完全接种人数",
    "加强针总数",
    "每日接种原始数据",
    "每日接种剂次",
    "每百人累计接种剂次",
    "每百人接种人数",
    "每百人完全接种人数",
    "每百人加强针剂次",
    "每百万人每日接种剂次",
    "每日新增接种人数",
    "每百人每日新增接种人数"
]
for col in columns_to_fill:
    if col in df.columns:
        df[col] = df[col].fillna(0)

# 输出看看结果
print(df.head())

# 保存处理结果
df.to_excel("../data/世界疫苗接种数据_cleaned.xlsx", index=False)
