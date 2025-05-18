import torch
import torch.nn as nn

class TransformerNet(nn.Module):
    def __init__(self, input_size, output_size, dim_model=64, num_heads=4, num_layers=2, dropout=0.1):
        super().__init__()
        self.input_proj = nn.Linear(input_size, dim_model)
        encoder_layer = nn.TransformerEncoderLayer(d_model=dim_model, nhead=num_heads, dropout=dropout, batch_first=True)
        self.transformer = nn.TransformerEncoder(encoder_layer, num_layers=num_layers)
        self.linear = nn.Linear(dim_model, output_size)

    def forward(self, x):  # x shape: (batch, seq_len, input_size)
        x = self.input_proj(x)  # 映射到transformer期望的维度
        y = self.transformer(x)  # y: (batch, seq_len, dim_model)
        return self.linear(y[:, -1, :])  # 取最后一个时间步的输出
