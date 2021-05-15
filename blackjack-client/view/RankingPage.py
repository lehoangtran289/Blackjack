from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs
import socket
from view import HomePage

class rankingPage(QtWidgets.QWidget):
    def __init__(self, socket, user, home):
        super().__init__()
        uic.loadUi('./ui/ranking.ui', self)
        self.user = user
        self.s = socket
        self.home_page = home
        self.s.sendall(b'GETRANKING')
        print('send GETRANKING')
        response = self.s.recv(1024)
        print('received: ' + response.decode('utf-8'))
        header, message = response.decode('utf-8').split('=')
        ranking_token = message.split(',')

        self.user_rank_label.setText(ranking_token[0].split(' ')[0])
        self.username_label.setText(ranking_token[0].split(' ')[1])
        self.user_gain_label.setText(ranking_token[0].split(' ')[2])

        ranking_token = ranking_token[1:]
        for token in ranking_token:
            rank, username, money = token.split(' ')
            rowPosition = self.ranking_table.rowCount()
            self.ranking_table.insertRow(rowPosition) 
            self.ranking_table.setItem(rowPosition - 1, 0, QtWidgets.QTableWidgetItem(rank)) 
            self.ranking_table.setItem(rowPosition - 1, 1, QtWidgets.QTableWidgetItem(username)) 
            self.ranking_table.setItem(rowPosition - 1, 2, QtWidgets.QTableWidgetItem(money)) 

        self.back_button.clicked.connect(self.back)

    def back(self):
        self.close()
        self.home_page.show()    



