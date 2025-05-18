import pandas as pd
import numpy as np
import torch
from models.TCN import TCN
from datetime import timedelta
import matplotlib.pyplot as plt
import os
import random

plt.rcParams['font.family'] = 'SimHei'  # 中文字体支持
plt.rcParams['axes.unicode_minus'] = False  # 负号正常显示

def predict_one_country(model, window, predict_steps=7):
    """
    使用训练好的模型预测单个国家未来predict_steps天的新增确诊和新增死亡人数。
    window: 标准化后的二维窗口数据，shape=(window_size, 2)
    """
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    model.eval()
    window = window.copy()
    pred_confirmed, pred_deaths = [], []

    for _ in range(predict_steps):
        # 输入形状 (1, seq_len, 2)
        x = torch.tensor(window, dtype=torch.float32).unsqueeze(0).to(device)
        with torch.no_grad():
            pred = model(x).cpu().numpy()[0]  # 输出shape (2,)
        pred_confirmed.append(pred[0])
        pred_deaths.append(pred[1])
        # 窗口向前滚动一行，最后一行更新为预测值
        window = np.roll(window, -1, axis=0)
        window[-1, 0] = pred[0]  # 新增确诊
        window[-1, 1] = pred[1]  # 新增死亡

    return np.array(pred_confirmed), np.array(pred_deaths)

def predict_all_countries(csv_path, model_path, output_path, window_size=14, predict_steps=7):
    """
    读取原始数据，针对每个国家预测未来predict_steps天数据，并保存预测结果。
    """
    print("pd module check:", pd)  # 调试用，确认pd是模块

    df = pd.read_csv(csv_path, parse_dates=['报告日期'])
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

    model = TCN(input_size=2, output_size=2, num_channels=[25, 25, 25, 25]).to(device)
    model.load_state_dict(torch.load(model_path, map_location=device))

    results = []

    for code, group in df.groupby('国家代码'):
        group = group.sort_values('报告日期')
        full_dates = pd.date_range(start=group['报告日期'].min(), end=group['报告日期'].max())
        group = group.set_index('报告日期').reindex(full_dates, fill_value=0).rename_axis('报告日期').reset_index()

        conf_series = group['新增确诊病例'].values
        death_series = group['新增死亡人数'].values

        conf_max = conf_series.max()
        death_max = death_series.max()

        if len(conf_series) < window_size or conf_max == 0 or death_max == 0:
            continue

        norm_conf = conf_series / conf_max
        norm_death = death_series / death_max

        window = np.stack([norm_conf[-window_size:], norm_death[-window_size:]], axis=1)

        pred_conf, pred_death = predict_one_country(model, window, predict_steps)

        pred_conf = pred_conf * conf_max
        pred_death = pred_death * death_max

        last_date = group['报告日期'].iloc[-1]
        pred_dates = [last_date + timedelta(days=i+1) for i in range(predict_steps)]

        for date, pc, pd_ in zip(pred_dates, pred_conf, pred_death):
            results.append({
                '国家代码': code,
                '预测日期': date.strftime('%Y-%m-%d'),
                '预测新增确诊病例': int(round(pc)),
                '预测新增死亡人数': int(round(pd_))
            })

    result_df = pd.DataFrame(results)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    result_df.to_csv(output_path, index=False)
    print(f"预测完成，结果保存至 {output_path}")


def visualize_predictions(csv_path, original_df, window_size=14, predict_steps=7, sample_size=10):
    """
    可视化部分：随机抽取若干国家，画出其历史数据与预测数据连续曲线，并在预测起点用虚线标注。
    """
    pred_df = pd.read_csv(csv_path, parse_dates=['预测日期'])
    sampled_countries = random.sample(pred_df['国家代码'].unique().tolist(), sample_size)

    os.makedirs('../data/visuals', exist_ok=True)

    for code in sampled_countries:
        orig_group = original_df[original_df['国家代码'] == code].sort_values('报告日期')
        full_dates = pd.date_range(start=orig_group['报告日期'].min(), end=orig_group['报告日期'].max())
        orig_group = orig_group.set_index('报告日期').reindex(full_dates, fill_value=0).rename_axis('报告日期').reset_index()

        conf_series = orig_group['新增确诊病例'].values
        death_series = orig_group['新增死亡人数'].values

        if len(conf_series) < window_size:
            continue

        # 过去window_size天数据
        recent_dates = orig_group['报告日期'].iloc[-window_size:]
        recent_conf = conf_series[-window_size:]
        recent_death = death_series[-window_size:]

        # 预测数据
        pred_group = pred_df[pred_df['国家代码'] == code].sort_values('预测日期')
        pred_conf = pred_group['预测新增确诊病例'].values
        pred_death = pred_group['预测新增死亡人数'].values
        pred_dates = pred_group['预测日期']

        # 拼接历史和预测，形成连续曲线
        full_conf = np.concatenate([recent_conf, pred_conf])
        full_death = np.concatenate([recent_death, pred_death])
        full_dates = pd.to_datetime(list(recent_dates) + list(pred_dates))

        plt.figure(figsize=(12, 5))

        plt.subplot(1, 2, 1)
        plt.plot(full_dates, full_conf, label='新增确诊（历史+预测）', color='purple', linewidth=2)
        plt.axvline(x=recent_dates.iloc[-1], color='gray', linestyle='--', label='预测开始')
        plt.title(f'国家代码: {code} 新增确诊预测')
        plt.xticks(rotation=45)
        plt.legend()

        plt.subplot(1, 2, 2)
        plt.plot(full_dates, full_death, label='新增死亡（历史+预测）', color='orange', linewidth=2)
        plt.axvline(x=recent_dates.iloc[-1], color='gray', linestyle='--', label='预测开始')
        plt.title(f'国家代码: {code} 新增死亡预测')
        plt.xticks(rotation=45)
        plt.legend()

        plt.tight_layout()
        plt.savefig(f'../data/visuals/{code}_forecast.png')
        plt.close()


if __name__ == '__main__':
    predict_all_countries(
        csv_path='../data/raw/test_data.csv',
        model_path='../logs/model_checkpoint.pth',
        output_path='../data/predicted/predictions.csv'
    )

    df = pd.read_csv('../data/raw/test_data.csv', parse_dates=['报告日期'])
    visualize_predictions(
        csv_path='../data/predicted/predictions.csv',
        original_df=df
    )
