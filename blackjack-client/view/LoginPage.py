from PyQt5 import QtCore, QtWidgets, QtGui, uic
from models.player import Player
from utils import configs
import socket
from view import StartPage, HomePage

class loginPage(QtWidgets.QMainWindow):
    def __init__(self, socket):
        super().__init__()
        uic.loadUi('./ui/login.ui', self)
        self.s = socket
        
        self.login_button.clicked.connect(self.login)
        self.back_button.clicked.connect(self.back_to_start_page)

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
            username, balance = msg_token[1].split(' ')
            self.home_page = HomePage.homePage(Player(username, password, balance), self.s)
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