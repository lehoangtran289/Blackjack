from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs
import socket
from view import StartPage

class signupPage(QtWidgets.QWidget):
    def __init__(self, socket):
        QtWidgets.QWidget.__init__(self)
        uic.loadUi('./ui/signup.ui', self)
        self.s = socket
        #self.username_entry = QtWidgets.QLineEdit()
        #self.password_entry = QtWidgets.QLineEdit()
        #self.password_entry.setEchoMode(QtWidgets.QLineEdit.Password)
        #self.setGeometry(100, 100, configs.WINDOW_WIDTH, configs.WINDOW_HEIGHT)

        #signup_button = QtWidgets.QPushButton('Signup')
        self.signup_button.clicked.connect(self.signup)
        #back_button = QtWidgets.QPushButton('Back')
        self.back_button.clicked.connect(self.back_to_start_page)

        #layout = QtWidgets.QGridLayout()
        #layout.addWidget(QtWidgets.QLabel('username'), 0, 0)
        #layout.addWidget(QtWidgets.QLabel('password'), 1, 0)
        #layout.addWidget(self.username_entry, 0, 1)
        #layout.addWidget(self.password_entry, 1, 1)
        #layout.addWidget(signup_button, 2, 0)
        #layout.addWidget(back_button, 2, 1)
        #self.setLayout(layout)

    def signup(self):
        username = self.username_entry.text()
        password = self.password_entry.text()
        confirm = self.confirm_entry.text()
        if username == '' or password == '':
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', 'Username and Password must not be empty!')
            return
        if password != confirm:
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', '2 passwords are not the same')
            return
        msg = 'SIGNUP ' + username + ' ' + password
        self.s.sendall(msg.encode())
        print("send: " + msg)
        response = self.s.recv(1024)
        self.validate_signup(response)

    def validate_signup(self, msg):
        msg_token = msg.decode('utf-8').split('=')
        if msg_token[0] == configs.SIGNUP_SUCCESS:
            print("Sign up Success")
            self.back_to_start_page()
        elif msg_token[0] == configs.SIGNUP_FAIL:
            print("Sign up failed")
            QtWidgets.QMessageBox.about('Sign up Failed', msg_token[1])
        else:
            print("Wrong message")

    def back_to_start_page(self):
        print("back to start page")
        self.close()
        self.start_page = StartPage.startPage(self.s)
        self.start_page.show()
        