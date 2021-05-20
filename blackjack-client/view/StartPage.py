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

    def show_login(self):
        self.close()
        self.login_page.show()

    def show_signup(self):
        self.close()
        self.signup_page.show()