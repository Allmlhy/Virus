import torch
from models.TCN import TCN
import numpy as np

def predict(model_path, input_sequence, predict_steps=7):
    """
    input_sequence: 1D numpy array of normalized recent time series data, length=window_size
    """
    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    model = TCN(input_size=1, output_size=1, num_channels=[25, 25, 25, 25]).to(device)
    model.load_state_dict(torch.load(model_path))
    model.eval()

    window = input_sequence.copy()
    predictions = []

    for _ in range(predict_steps):
        x = torch.tensor(window, dtype=torch.float32).unsqueeze(0).unsqueeze(-1).to(device)  # (1, window_size, 1)
        with torch.no_grad():
            pred = model(x).item()
        predictions.append(pred)
        window = np.roll(window, -1)
        window[-1] = pred

    return np.array(predictions)

if __name__ == '__main__':
    # 示例
    sample_input = np.random.rand(14)  # 真实用法是你归一化的时间序列片段
    preds = predict('../logs/model_checkpoint.pth', sample_input, predict_steps=7)
    print('Predictions:', preds)
