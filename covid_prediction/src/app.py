from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from fastapi.exception_handlers import request_validation_exception_handler
from pydantic import BaseModel
from typing import List
import torch
import numpy as np
from datetime import datetime, timedelta
from models.TCN import TCN
from models.LSTM import LSTMNet
from models.GRU import GRUNet
from models.Transformer import TransformerNet
# -------------------------------
# 输入输出模型
class InputDataItem(BaseModel):
    date: str          # "YYYY-MM-DD"
    newConfirmed: int
    newDeaths: int

class OutputDataItem(BaseModel):
    date: str
    predictedConfirmed: int
    predictedDeaths: int

# -------------------------------
# 初始化应用
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    print(f"[ValidationError] 请求数据校验失败: {exc.errors()}")
    return await request_validation_exception_handler(request, exc)

# -------------------------------
# 预加载模型
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print(f"[startup] Using device: {device}")

tcn_model = TCN(input_size=2, output_size=2, num_channels=[25, 25, 25, 25]).to(device)
tcn_model.load_state_dict(torch.load('../logs/model_checkpoint.pth', map_location=device))
tcn_model.eval()
print("[startup] TCN model loaded.")

lstm_model = LSTMNet(input_size=2, output_size=2).to(device)
lstm_model.load_state_dict(torch.load('../logs/model_LSTM_checkpoint.pth', map_location=device))
lstm_model.eval()
print("[startup] LSTM model loaded.")

gru_model = GRUNet(input_size=2, output_size=2).to(device)
gru_model.load_state_dict(torch.load('../logs/model_GRU_checkpoint.pth', map_location=device))
gru_model.eval()
print("[startup] GRU model loaded.")

transformer_model = TransformerNet(input_size=2, output_size=2).to(device)
transformer_model.load_state_dict(torch.load('../logs/model_Transformer_checkpoint.pth', map_location=device))
transformer_model.eval()
print("[startup] Transformer model loaded.")


# -------------------------------
# 通用预测逻辑
def predict_one_country(model, window, predict_steps=7):
    model.eval()
    pred_conf = []
    pred_death = []
    device = next(model.parameters()).device

    seq = window.copy()
    seq = torch.tensor(seq, dtype=torch.float32).unsqueeze(0).to(device)

    for step in range(predict_steps):
        with torch.no_grad():
            out = model(seq)
            conf_pred, death_pred = out[0, 0].item(), out[0, 1].item()
            pred_conf.append(conf_pred)
            pred_death.append(death_pred)

        new_day = torch.tensor([[conf_pred, death_pred]], dtype=torch.float32).unsqueeze(0).to(device)
        seq = torch.cat([seq[:, 1:, :], new_day], dim=1)

    return np.array(pred_conf), np.array(pred_death)

# -------------------------------
# 通用数据处理和预测流程
def handle_prediction_request(data: List[InputDataItem], model) -> List[OutputDataItem]:
    if len(data) < 14:
        raise HTTPException(status_code=400, detail="输入数据长度不足14条")

    try:
        data_sorted = sorted(data, key=lambda x: datetime.strptime(x.date, "%Y-%m-%d"))
        dates = [datetime.strptime(item.date, "%Y-%m-%d") for item in data_sorted]
    except Exception as e:
        raise HTTPException(status_code=400, detail="日期格式错误，应为YYYY-MM-DD")

    conf_series = np.array([item.newConfirmed for item in data_sorted], dtype=float)
    death_series = np.array([item.newDeaths for item in data_sorted], dtype=float)

    conf_max = conf_series.max()
    death_max = death_series.max()

    if conf_max == 0 and death_max == 0:
        last_date = dates[-1]
        return [
            OutputDataItem(
                date=(last_date + timedelta(days=i+1)).strftime("%Y-%m-%d"),
                predictedConfirmed=0,
                predictedDeaths=0
            ) for i in range(7)
        ]

    norm_conf = conf_series / conf_max if conf_max != 0 else conf_series
    norm_death = death_series / death_max if death_max != 0 else death_series

    window_size = 14
    window = np.stack([norm_conf[-window_size:], norm_death[-window_size:]], axis=1)

    try:
        pred_conf, pred_death = predict_one_country(model, window, predict_steps=7)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"模型预测失败: {str(e)}")

    pred_conf = pred_conf * conf_max if conf_max != 0 else pred_conf
    pred_death = pred_death * death_max if death_max != 0 else pred_death

    last_date = dates[-1]
    return [
        OutputDataItem(
            date=(last_date + timedelta(days=i+1)).strftime("%Y-%m-%d"),
            predictedConfirmed=int(round(pred_conf[i])),
            predictedDeaths=int(round(pred_death[i]))
        )
        for i in range(7)
    ]

# -------------------------------
# 接口：TCN模型预测
@app.post("/predict_tcn/", response_model=List[OutputDataItem])
async def predict_tcn(data: List[InputDataItem]):
    return handle_prediction_request(data, tcn_model)

# 接口：LSTM模型预测
@app.post("/predict_lstm/", response_model=List[OutputDataItem])
async def predict_lstm(data: List[InputDataItem]):
    return handle_prediction_request(data, lstm_model)

@app.post("/predict_gru/", response_model=List[OutputDataItem])
async def predict_gru(data: List[InputDataItem]):
    return handle_prediction_request(data, gru_model)

@app.post("/predict_transformer/", response_model=List[OutputDataItem])
async def predict_transformer(data: List[InputDataItem]):
    return handle_prediction_request(data, transformer_model)
