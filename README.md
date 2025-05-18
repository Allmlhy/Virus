COVID-19 预测接口服务
=======================

基于 FastAPI 框架，集成了多种时间序列深度学习模型（TCN、LSTM、GRU、Transformer），用于预测未来 7 天的新增确诊人数和新增死亡人数。用户需提交至少连续 14 天的历史数据作为输入，系统将返回接下来的 7 天预测值。

项目结构
--------

.
├── main.py                        # FastAPI 接口主文件
├── models/
│   ├── TCN.py                     # Temporal Convolutional Network 模型定义
│   ├── LSTM.py                    # LSTM 模型定义
│   ├── GRU.py                     # GRU 模型定义
│   └── Transformer.py             # Transformer 模型定义
├── logs/
│   ├── model_checkpoint.pth               # TCN 模型权重
│   ├── model_LSTM_checkpoint.pth          # LSTM 模型权重
│   ├── model_GRU_checkpoint.pth           # GRU 模型权重
│   └── model_Transformer_checkpoint.pth   # Transformer 模型权重

依赖安装
--------

使用 pip 安装必要依赖：

    pip install fastapi uvicorn torch numpy pydantic

启动服务
--------

在项目根目录运行以下命令启动接口服务：

    uvicorn main:app --reload

默认服务地址为：

    http://127.0.0.1:8000

接口说明
--------

1. 输入格式（适用于所有模型）：

    [
      {
        "date": "2023-01-01",
        "newConfirmed": 100,
        "newDeaths": 2
      },
      ...
    ]

    - 输入至少为 14 条记录
    - 日期格式必须为 YYYY-MM-DD

2. 输出格式（预测结果）：

    [
      {
        "date": "2023-01-15",
        "predictedConfirmed": 120,
        "predictedDeaths": 3
      },
      ...
    ]

3. 接口路径：

    - /predict_tcn/         使用 TCN 模型预测
    - /predict_lstm/        使用 LSTM 模型预测
    - /predict_gru/         使用 GRU 模型预测
    - /predict_transformer/ 使用 Transformer 模型预测

使用示例
--------

使用 curl 提交 POST 请求（以 TCN 为例）：

    curl -X POST http://127.0.0.1:8000/predict_tcn/ \
    -H "Content-Type: application/json" \
    -d @input.json

其中 input.json 文件内容如下：

    [
      { "date": "2023-01-01", "newConfirmed": 100, "newDeaths": 1 },
      { "date": "2023-01-02", "newConfirmed": 120, "newDeaths": 0 },
      ...
      { "date": "2023-01-14", "newConfirmed": 80,  "newDeaths": 2 }
    ]

错误处理
--------

- 如果输入数据不足 14 条，返回 400 错误
- 如果日期格式不正确，返回 400 错误
- 模型推理错误将返回 500 错误

其他说明
--------

- 所有模型预加载于服务启动时，保存在 logs 目录下
- 预测采用滑动窗口方式，仅使用最新 14 天数据
- 所有数据将自动归一化并反归一化输出结果
