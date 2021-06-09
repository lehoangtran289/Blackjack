from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class aboutPage(QtWidgets.QWidget):
    def __init__(self, user, connection, x, y):
        super().__init__()
        uic.loadUi('./ui/about.ui', self)
        self.user = user
        self.connection = connection
        self.back_button.clicked.connect(self.back)
        self.close_on_purpose = True
        self.contributors.setText(configs.contributors)
        self.rules.setText(configs.rules)
        self.setWindowTitle('About')
        self.setFixedSize(800, 600)
        self.setGeometry(x, y, 800, 600)

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