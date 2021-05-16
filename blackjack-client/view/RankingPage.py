from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class rankingPage(QtWidgets.QWidget):
    def __init__(self, user, connection, home):
        super().__init__()
        uic.loadUi('./ui/ranking.ui', self)
        self.user = user
        self.connection = connection
        self.home_page = home
        self.ranking_table.setColumnWidth(0, 50)
        self.ranking_table.setColumnWidth(1, 200)
        self.ranking_table.setColumnWidth(2, 200)
        request = 'GETRANKING ' + self.user.username 
        response = self.connection.send_request(request)
        header, message = response.split('=')
        ranking_token = message.split(',')

        self.user_rank_label.setText(ranking_token[0].split(' ')[0])
        self.username_label.setText(ranking_token[0].split(' ')[1])
        self.user_gain_label.setText(ranking_token[0].split(' ')[2])

        ranking_token = ranking_token[1:]
        row = 0
        for token in ranking_token:
            rank, username, money = token.split(' ') 
            self.ranking_table.insertRow(row)
            self.ranking_table.setItem(row, 0, QtWidgets.QTableWidgetItem(rank)) 
            self.ranking_table.setItem(row, 1, QtWidgets.QTableWidgetItem(username)) 
            self.ranking_table.setItem(row, 2, QtWidgets.QTableWidgetItem(money)) 
            row = row + 1

        self.back_button.clicked.connect(self.back)

    def back(self):
        self.close()
        self.home_page.show()    



