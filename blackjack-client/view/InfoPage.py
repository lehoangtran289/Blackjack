from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class infoPage(QtWidgets.QWidget):
    def __init__(self, user, connection, home):
        super().__init__()
        uic.loadUi('./ui/player_info.ui', self)
        self.user = user
        self.connection = connection
        self.home_page = home
        self.setWindowTitle('Player information')
        self.setFixedSize(640, 480)
        self.result_table.setColumnWidth(0, 100)
        self.result_table.setColumnWidth(1, 100)
        self.result_table.setColumnWidth(2, 100)
        self.result_table.setColumnWidth(3, 60)
        self.result_table.setColumnWidth(4, 60)
        self.result_table.setColumnWidth(5, 60)
        self.result_table.setColumnWidth(6, 60)
        self.result_table.setColumnWidth(7, 98)
        self.search_button.clicked.connect(self.search)
        self.search_entry.returnPressed.connect(self.search_button.click)
        self.close_on_purpose = True
        
        request = 'INFO ' + self.user.username
        response = self.connection.send_request(request)
        header = self.connection.get_header(response)
        message = self.connection.get_message(response)
        
        if header == 'INFO':
            stats = message.split(' ')
            self.username.setText(stats[0])
            self.balance.setText(stats[1])
            self.gain_lose.setText(stats[2])
            self.win.setText(stats[3])
            self.lose.setText(stats[4])
            self.push.setText(stats[5])
            self.bust.setText(stats[6])
            self.blackjack.setText(stats[7])
        else:
            QtWidgets.QMessageBox.about(self, 'Failed', message)

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

    def search(self):
        keyword = self.search_entry.text()
        request = 'SEARCHINFO ' + keyword
        response = self.connection.send_request(request)
        header = response.split('=')[0]
        self.result_table.setRowCount(0)
        if header == 'SEARCHSUCCESS':
            row = 0
            _, message = response.split('=')
            print(message)
            for token in message.split(','):
                stats = token.split(' ')
                self.result_table.insertRow(row)
                for i in range(len(stats)):
                    self.result_table.setItem(row, i, QtWidgets.QTableWidgetItem(stats[i]))
                    self.result_table.item(row, i).setTextAlignment(QtCore.Qt.AlignCenter)
                row += 1
        else:
            self.result_table.insertRow(0)
            self.result_table.setSpan(0, 0, 1, 8)
            self.result_table.setItem(0, 0, QtWidgets.QTableWidgetItem('Username not found'))

    def back(self): 
        self.close_on_purpose = False
        self.close()
        self.home_page.show()