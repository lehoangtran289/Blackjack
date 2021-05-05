from PyQt5 import QtCore, QtWidgets, QtGui
from models.player import Player
from utils import configs
import socket
from view import StartPage, HomePage

class loginPage(QtWidgets.QWidget):
    def __init__(self, socket):
        QtWidgets.QWidget.__init__(self)
        self.s = socket
        self.username_entry = QtWidgets.QLineEdit()
        self.password_entry = QtWidgets.QLineEdit()
        self.password_entry.setEchoMode(QtWidgets.QLineEdit.Password)
        self.setGeometry(100, 100, configs.WINDOW_WIDTH, configs.WINDOW_HEIGHT)

        login_button = QtWidgets.QPushButton('Login')
        login_button.clicked.connect(self.login)
        back_button = QtWidgets.QPushButton('Back')
        back_button.clicked.connect(self.back_to_start_page)
        

        layout = QtWidgets.QGridLayout()
        layout.addWidget(QtWidgets.QLabel('username'), 0, 0)
        layout.addWidget(QtWidgets.QLabel('password'), 1, 0)
        layout.addWidget(self.username_entry, 0, 1)
        layout.addWidget(self.password_entry, 1, 1)
        layout.addWidget(login_button, 2, 0)
        layout.addWidget(back_button, 2, 1)
        self.setLayout(layout)

    def login(self):
        username = self.username_entry.text()
        password = self.password_entry.text()
        if username == '' or password == '':
            QtWidgets.QMessageBox.about('Sign up Failed', 'Username and Password must not be empty!')
            return
        msg = 'LOGIN ' + username + ' ' + password
        print("send: " + msg)
        self.s.sendall(msg.encode())
        response = self.s.recv(1024)
        self.validate_login(response, username, password)

    def validate_login(self, msg, username, password):
        msg_token = msg.decode('utf-8').split('=')
        if msg_token[0] == configs.LOGIN_SUCCESS:
            self.home_page = HomePage.homePage(Player(username, password, 10000), self.s)
            self.close()
            self.home_page.show()
            print("login success, go to home page")
        elif msg_token[0] == configs.LOGIN_FAIL:
            print("log in failed")
            QtWidgets.QMessageBox.about('Log in Failed', msg_token[1])
        else:
            print("Wrong message")

    def back_to_start_page(self):
        print("back to start page")
        self.close()
        self.start_page = StartPage.startPage(self.s)
        self.start_page.show()