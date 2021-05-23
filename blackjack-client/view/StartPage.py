from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import LoginPage, SignupPage

class startPage(QtWidgets.QMainWindow):
    def __init__(self, connection):
        super().__init__()
        uic.loadUi('./ui/start.ui', self)

        self.connection = connection
        self.login_page = LoginPage.loginPage(self.connection)
        self.signup_page = SignupPage.signupPage(self.connection)
        self.login_button.clicked.connect(self.show_login)
        self.signup_button.clicked.connect(self.show_signup)
        self.setWindowTitle('BlackJack')
        self.setFixedSize(640, 480)
        self.close_on_purpose = True
    
    def closeEvent(self, event):
        if self.close_on_purpose == False:
            event.accept()
            return
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit?', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            event.accept()
        else:
            event.ignore()

    def show_login(self):
        self.close_on_purpose = False
        self.close()
        self.login_page.show()

    def show_signup(self):
        self.close_on_purpose = False
        self.close()
        self.signup_page.show()