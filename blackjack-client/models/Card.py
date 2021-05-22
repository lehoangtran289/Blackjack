from PyQt5 import QtCore, QtWidgets, QtGui
from utils import configs

class card():
    def __init__(self, rank, suit):
        self.rank = rank
        self.suit = suit

    def display(self, layout):
        label = QtWidgets.QLabel()
        if self.rank == '?' and self.suit == '?':
            path = './asset/cards/card_back.png'
        else:
            path = "./asset/cards/" + self.rank + '_of_' + self.suit + '.png'
        label.setStyleSheet("border-image : url(" + path + ") 0 0 0 0 stretch stretch;")
        layout.addWidget(label)
