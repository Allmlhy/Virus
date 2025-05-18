import torch
import torch.nn as nn

class LSTMNet(nn.Module):
    def __init__(self, input_size, output_size, hidden_size=64, num_layers=2, dropout=0.2):
        super().__init__()
        self.lstm = nn.LSTM(input_size=input_size, hidden_size=hidden_size, num_layers=num_layers,
                            dropout=dropout, batch_first=True)
        self.linear = nn.Linear(hidden_size, output_size)

    def forward(self, x):  # x shape: (batch, seq_len, input_size)
        _, (hn, _) = self.lstm(x)  # hn: (num_layers, batch, hidden_size)
        return self.linear(hn[-1])  # 取最后一层的隐藏状态
