from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs
import socket
from view import LoginPage, SignupPage

class startPage(QtWidgets.QMainWindow):
    def __init__(self, socket):
        super().__init__()
        uic.loadUi('./ui/start.ui', self)
        #self.centralwidget = QtWidgets.QWidget()
        #self.setCentralWidget(self.centralwidget)

        self.s = socket
        self.login_page = LoginPage.loginPage(self.s)
        self.signup_page = SignupPage.signupPage(self.s)

        #self.setGeometry(100, 100, configs.WINDOW_WIDTH, configs.WINDOW_HEIGHT)
    
        #self.login_button = QtWidgets.QPushButton('Log in', self.centralwidget)
        self.login_button.clicked.connect(self.show_login)
        #self.login_button.setGeometry(300, 400, 200, 50)
        #self.signup_button = QtWidgets.QPushButton('Sign up', self.centralwidget)
        self.signup_button.clicked.connect(self.show_signup)
        #self.signup_button.setGeometry(300, 450, 200, 50)

        #label = QtWidgets.QLabel('Welcome to BlackJack', self.centralwidget)
        #label.setFont(QtGui.QFont('Arial', 20))
        #label.setGeometry(300, 100, 400, 100)

    def show_login(self):
        print("go to login page")
        self.close()
        self.login_page.show()

    def show_signup(self):
        print("go to sign up page")
        self.close()
        self.signup_page.show()