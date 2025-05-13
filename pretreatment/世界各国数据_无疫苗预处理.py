import pandas as pd

# 1. 读取 Excel 表格
file_path = "../data/世界各国数据_无疫苗.xlsx"
df = pd.read_excel(file_path)

# 1. 删除整列都是空值的列
df.dropna(axis=1, how='all', inplace=True)

# 3. 将“日期”列转换为标准 datetime 类型，再格式化为“2020-1-20”样式
df['报告日期'] = pd.to_datetime(df['报告日期'], errors='coerce')
df['报告日期'] = df['报告日期'].dt.date  # 只保留日期部分

# 5. 填充指定列中的空值为 0
columns_to_fill = ['新增确诊病例','累计确诊病例','新增死亡人数','累计死亡人数']
for col in columns_to_fill:
    if col in df.columns:
        df[col] = df[col].fillna(0)

# 输出看看结果
print(df.head())

# 保存处理结果
df.to_excel("../data/世界各国数据_无疫苗_cleaned.xlsx", index=False)
