from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class aboutPage(QtWidgets.QWidget):
    def __init__(self, home):
        super().__init__()
        uic.loadUi('./ui/about.ui', self)
        self.back_button.clicked.connect(self.back)
        self.close_on_purpose = True
        self.contributors.setText(configs.contributors)
        self.rules.setText(configs.rules)
        self.setWindowTitle('About')
        self.setFixedSize(640, 480)
        self.home_page = home

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