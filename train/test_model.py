from __future__ import print_function

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from PIL import Image
import torchvision.transforms as transforms
import torchvision.models as models
import csv
import pandas as pd


device = torch.device("cuda" if torch.cuda.is_available() else "cpu")


class Model(nn.Module):
    def __init__(self):
        super(Model, self).__init__()
        self.lstm = nn.LSTM(3, 3, 2, bidirectional=True, batch_first=True)
        self.linear = nn.Linear(6, 1)
        self.sigmoid = nn.Sigmoid()

    def forward(self, x):
        h_n, c_n = self.lstm(x)
        last = h_n[:, -1, :]
        x = self.linear(last)
        x = torch.reshape(x, (-1,))
        x = self.sigmoid(x)
        return x

model = Model()
# print(model)
loss = nn.BCELoss()
optimizer = optim.SGD(model.parameters(), lr=0.1)



dataframe = pd.read_csv("./EmoSwipeData.csv", delimiter=";")
max_len = 30
labels = []
for lb in dataframe['Emotion']:
    if lb == 'P':
        labels.append(1)
    else:
        labels.append(0)
sequence = []
for sq in dataframe['Trajectory']:
    this_seq = sq.split('>,')
    this_sequence = []
    for item in this_seq:    # item is : <x,y,t
        if item == '':
            break
        item = item[1:]
        item = item.split(',')
        x = float(item[0])
        y = float(item[1])
        t = float(item[2])
        if len(this_sequence) < max_len-1:
            this_sequence.append((x, y, t))
        else:
            break
    this_sequence.append((-1, -1, -1))
    while len(this_sequence) < max_len:
        this_sequence.append((0.0, 0.0, 0.0))
    sequence.append(this_sequence)

train_sequence = sequence[:400]
val_sequence = sequence[400:]
train_labels = labels[:400]
val_labels = labels[400:]

train_sequence = torch.FloatTensor(train_sequence).to(device)
val_sequence = torch.FloatTensor(val_sequence).to(device)
train_labels = torch.FloatTensor(train_labels).to(device)
val_labels = torch.FloatTensor(val_labels).to(device)

n_epochs = 100
batch_size = 16
for epoch in range(n_epochs):
    permutation = torch.randperm(train_sequence.size()[0])
    epoch_loss = 0.0
    model.train()
    for i in range(0, train_sequence.size()[0], batch_size):
        optimizer.zero_grad()
        indices = permutation[i:i+batch_size]
        batch_x, batch_y = train_sequence[indices], train_labels[indices]
        outputs = model.forward(batch_x)
        diff = loss(outputs, batch_y)
        diff.backward()
        optimizer.step()
        epoch_loss = epoch_loss + diff
    epoch_loss = epoch_loss / (train_sequence.size()[0] / batch_size)
    print("Epoch {:.2f}, Loss {:.4f}".format(epoch, epoch_loss))

    permutation_val = torch.randperm(val_sequence.size()[0])
    val_loss = 0.0
    tp = 0.0
    tn = 0.0
    fp = 0.0
    fn = 0.0
    model.eval()
    for i in range(0, val_sequence.size()[0], batch_size):
        indices = permutation_val[i:i + batch_size]
        batch_x, batch_y = val_sequence[indices], val_labels[indices]
        outputs = model.forward(batch_x)
        diff = loss(outputs, batch_y)
        val_loss = val_loss + diff
        for a, b in zip(outputs, batch_y):
            if a > 0.5 and b == 1:
                tp = tp + 1
            elif a > 0.5 and b == 0:
                fp = fp + 1
            elif a <= 0.5 and b == 1:
                fn = fn + 1
            elif a <= 0.5 and b == 0:
                tn = tn + 1
    val_loss = val_loss / (val_sequence.size()[0] / batch_size)
    val_acc = (tp+tn) / (tp + tn + fp + fn)
    precision = tp / (tp + fp + 0.001)
    recall = tp / (tp + fn + 0.001)
    f1 = 2*precision*recall / (precision + recall + 0.001)
    print("Validation at Epoch {:.2f}: Loss {:.4f}, Acc {:.4f}, F1 {:.4f}".format(epoch, val_loss, val_acc, f1))

torch.save(model, "model.pt")
