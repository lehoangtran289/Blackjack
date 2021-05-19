from PyQt5 import QtCore, QtWidgets, QtGui
from utils import configs

class card():
    def __init__(self, rank, suit):
        self.rank = rank
        self.suit = suit

    def display_card(self, x, y, page):
        label = QtWidgets.QLabel(page)
        label.setText(self.rank + self.suit)
        label.setGeometry(x, y, configs.card_height, configs.card_width)