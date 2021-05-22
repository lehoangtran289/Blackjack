from PyQt5 import QtCore, QtWidgets, QtGui
from utils import configs

class card():
    def __init__(self, rank, suit):
        self.rank = rank
        self.suit = suit

    def display(self, x, y, page):
        label = QtWidgets.QLabel(page)
        label.setText(self.rank + self.suit)
        label.move(x, y)
        print("here")
        label.resize(configs.card_height, configs.card_width)
        if self.rank == '?' and self.suit == '?':
            path = './asset/cards/cardback.png'
        else:
            path = "./asset/cards/" + self.rank + '_of_' + self.suit + '.png'
        label.setStyleSheet("background-image : url(" + path + ")")