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
        self.setWindowTitle('Ranking')
        self.setFixedSize(640, 480)
        self.close_on_purpose = True
        request = 'GETRANKING ' + self.user.username 
        response = self.connection.send_request(request)
        header = self.connection.get_header(response)
        message = self.connection.get_message(response)
        ranking_token = message.split(',')

        self.user_rank_label.setText(ranking_token[0].split(' ')[0])
        self.username_label.setText(ranking_token[0].split(' ')[1])
        self.user_gain_label.setText(ranking_token[0].split(' ')[2])

        ranking_token = ranking_token[1:]
        row = 0
        for token in ranking_token:
            stats = token.split(' ') 
            self.ranking_table.insertRow(row)
            for i in range(len(stats)):
                self.ranking_table.setItem(row, i, QtWidgets.QTableWidgetItem(stats[i])) 
                self.ranking_table.item(row, i).setTextAlignment(QtCore.Qt.AlignCenter)
            row = row + 1

        self.back_button.clicked.connect(self.back)

    def closeEvent(self, event):
        if self.close_on_purpose == False:
            event.accept()
            return
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit?', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            request = 'LOGOUT ' + self.user.username
            self.connection.send(request)
            event.accept()
        else:
            event.ignore()

    def back(self):
        self.close_on_purpose = False
        self.close()
        self.home_page.show()    



