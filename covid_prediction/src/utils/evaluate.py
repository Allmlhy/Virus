import torch
from torch.utils.data import DataLoader, TensorDataset
from models.TCN import TCN

def evaluate(model_path='../logs/model_checkpoint.pth',
             val_path='../data/processed/val_data.pt'):

    device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    X_val, y_val = torch.load(val_path)

    val_dataset = TensorDataset(X_val, y_val)
    val_loader = DataLoader(val_dataset, batch_size=64)

    model = TCN(input_size=2, output_size=2, num_channels=[25, 25, 25, 25]).to(device)
    model.load_state_dict(torch.load(model_path))
    model.eval()

    criterion = torch.nn.MSELoss()
    val_loss = 0
    with torch.no_grad():
        for X_batch, y_batch in val_loader:
            X_batch, y_batch = X_batch.to(device), y_batch.to(device)
            output = model(X_batch.unsqueeze(-1))
            loss = criterion(output, y_batch)
            val_loss += loss.item()
    val_loss /= len(val_loader)
    print(f'Validation Loss: {val_loss:.6f}')
    return val_loss

if __name__ == '__main__':
    evaluate()
