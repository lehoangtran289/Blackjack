from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class historyPage(QtWidgets.QWidget):
    def __init__(self, user, connection, home):
        super().__init__()
        uic.loadUi('./ui/history.ui', self)
        self.user = user
        self.connection = connection
        self.home_page = home
        self.back_button.clicked.connect(self.back)
        self.history_table.setColumnWidth(0, 200)
        self.history_table.setColumnWidth(1, 200)
        self.history_table.setColumnWidth(2, 200)

        request = 'HISTORY ' + self.user.username
        response = self.connection.send_request(request)
        header, message = response.split('=')

        if header == 'HISTORY':
            row = 0
            for token in message.split(','):
                self.history_table.insertRow(row)
                data = token.split(' ')
                for i in range(len(data)):
                    self.history_table.setItem(row, i, QtWidgets.QTableWidgetItem(data[i]))


    def back(self):
        self.close()
        self.home_page.show()