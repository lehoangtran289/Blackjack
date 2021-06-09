from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class historyPage(QtWidgets.QWidget):
    def __init__(self, user, connection, x, y):
        super().__init__()
        uic.loadUi('./ui/history.ui', self)
        self.user = user
        self.connection = connection
        self.back_button.clicked.connect(self.back)
        self.history_table.setColumnWidth(0, 200)
        self.history_table.setColumnWidth(1, 200)
        self.history_table.setColumnWidth(2, 200)
        self.setWindowTitle('Playing History')
        self.setFixedSize(800, 600)
        self.setGeometry(x, y, 800, 600)
        self.close_on_purpose = True

        request = 'HISTORY ' + self.user.username
        response = self.connection.send_request(request)
        header = self.connection.get_header(response)
        message = self.connection.get_message(response)

        if header == 'HISTORY':
            row = 0
            for token in message.split(','):
                self.history_table.insertRow(row)
                data = token.split(' ')
                for i in range(len(data)):
                    self.history_table.setItem(row, i, QtWidgets.QTableWidgetItem(data[i]))
                    self.history_table.item(row, i).setTextAlignment(QtCore.Qt.AlignCenter)

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
        self.home_page = HomePage.homePage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.home_page.show()