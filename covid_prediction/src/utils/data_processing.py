import pandas as pd
import numpy as np
import torch
from sklearn.model_selection import train_test_split
import os

def load_and_process(csv_path, window_size=14, test_size=0.2, random_state=42):
    df = pd.read_csv(csv_path, parse_dates=['报告日期'])
    groups = df.groupby('国家代码')
    
    X_all, y_all = [], []

    for country, group in groups:
        group = group.sort_values('报告日期')
        full_dates = pd.date_range(start=group['报告日期'].min(), end=group['报告日期'].max())
        group = group.set_index('报告日期').reindex(full_dates, fill_value=0).rename_axis('报告日期').reset_index()
        
        conf_series = group['新增确诊病例'].values.astype(np.float32)
        death_series = group['新增死亡人数'].values.astype(np.float32)

        conf_max = conf_series.max()
        death_max = death_series.max()
        if conf_max == 0 or death_max == 0:
            continue

        conf_series = conf_series / conf_max
        death_series = death_series / death_max

        for i in range(len(conf_series) - window_size):
            # 输入同时包含两个特征
            window_conf = conf_series[i:i+window_size]
            window_death = death_series[i:i+window_size]
            window_features = np.stack([window_conf, window_death], axis=1)  # shape: (window_size, 2)
            X_all.append(window_features)
            
            # 预测目标是下一天的两个指标值
            y_all.append([conf_series[i+window_size], death_series[i+window_size]])

    X_all = np.array(X_all)
    y_all = np.array(y_all)

    X_train, X_val, y_train, y_val = train_test_split(X_all, y_all, test_size=test_size, random_state=random_state)

    os.makedirs('../data/processed', exist_ok=True)
    torch.save((torch.tensor(X_train, dtype=torch.float32), torch.tensor(y_train, dtype=torch.float32)), '../data/processed/train_data.pt')
    torch.save((torch.tensor(X_val, dtype=torch.float32), torch.tensor(y_val, dtype=torch.float32)), '../data/processed/val_data.pt')

    print(f"Train samples: {len(X_train)}, Validation samples: {len(X_val)}")

if __name__ == '__main__':
    load_and_process('../data/raw/covid_data.csv')
