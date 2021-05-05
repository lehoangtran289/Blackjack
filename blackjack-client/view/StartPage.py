from PyQt5 import QtCore, QtWidgets, QtGui
from utils import configs
import socket
from view import LoginPage, SignupPage

class startPage(QtWidgets.QWidget):
    def __init__(self, socket):
        QtWidgets.QWidget.__init__(self)
        layout = QtWidgets.QGridLayout()
        self.s = socket
        self.login_page = LoginPage.loginPage(self.s)
        self.signup_page = SignupPage.signupPage(self.s)

        self.setGeometry(100, 100, configs.WINDOW_WIDTH, configs.WINDOW_HEIGHT)
        self.setStyleSheet('background-image: url("./asset/home.png")')
    
        self.login_button = QtWidgets.QPushButton('Log in', self)
        self.login_button.clicked.connect(self.show_login)
        self.login_button.setGeometry(300, 400, 200, 50)
        self.signup_button = QtWidgets.QPushButton('Sign up', self)
        self.signup_button.clicked.connect(self.show_signup)
        self.signup_button.setGeometry(300, 450, 200, 50)

        label = QtWidgets.QLabel('Welcome to BlackJack', self)
        label.setFont(QtGui.QFont('Arial', 20))
        label.setGeometry(300, 100, 400, 100)

    def show_login(self):
        print("go to login page")
        self.close()
        self.login_page.show()

    def show_signup(self):
        print("go to sign up page")
        self.close()
        self.signup_page.show()