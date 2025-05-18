import torch
from torch.utils.data import DataLoader, TensorDataset
from models.TCN import TCN  # 确保你的model.py中TCN模型能接受input_size参数
from models.LSTM import LSTMNet
from models.GRU import GRUNet
from models.Transformer import TransformerNet
def train_model(train_path='../data/processed/train_data.pt',
                val_path='../data/processed/val_data.pt',
                epochs=30, batch_size=64, lr=0.001, save_path='../logs/model_Transformer_checkpoint.pth'):

    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    X_train, y_train = torch.load(train_path)
    X_val, y_val = torch.load(val_path)

    train_dataset = TensorDataset(X_train, y_train)
    val_dataset = TensorDataset(X_val, y_val)

    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    val_loader = DataLoader(val_dataset, batch_size=batch_size)

    # 输入特征数改为2
    # model = TCN(input_size=2, output_size=2, num_channels=[25, 25, 25, 25]).to(device)
    # model = LSTMNet(input_size=2, output_size=2).to(device)
    # model = GRUNet(input_size=2, output_size=2).to(device)
    model = TransformerNet(input_size=2, output_size=2).to(device)
    optimizer = torch.optim.Adam(model.parameters(), lr=lr)
    criterion = torch.nn.MSELoss()

    best_val_loss = float('inf')

    for epoch in range(epochs):
        model.train()
        train_loss = 0
        for X_batch, y_batch in train_loader:
            X_batch, y_batch = X_batch.to(device), y_batch.to(device)
            optimizer.zero_grad()
            output = model(X_batch)  # 输入已经是 (batch, seq_len, 2)
            loss = criterion(output, y_batch)
            loss.backward()
            optimizer.step()
            train_loss += loss.item()

        val_loss = 0
        model.eval()
        with torch.no_grad():
            for X_batch, y_batch in val_loader:
                X_batch, y_batch = X_batch.to(device), y_batch.to(device)
                output = model(X_batch)
                loss = criterion(output, y_batch)
                val_loss += loss.item()

        train_loss /= len(train_loader)
        val_loss /= len(val_loader)
        print(f'Epoch {epoch+1}/{epochs}, Train Loss: {train_loss:.6f}, Val Loss: {val_loss:.6f}')

        if val_loss < best_val_loss:
            best_val_loss = val_loss
            torch.save(model.state_dict(), save_path)
            print(f"Saved best model with val loss {best_val_loss:.6f}")

if __name__ == '__main__':
    train_model()
