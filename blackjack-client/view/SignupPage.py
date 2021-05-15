from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import StartPage

class signupPage(QtWidgets.QWidget):
    def __init__(self, connection):
        QtWidgets.QWidget.__init__(self)
        uic.loadUi('./ui/signup.ui', self)
        self.connection = connection
        self.signup_button.clicked.connect(self.signup)
        self.back_button.clicked.connect(self.back_to_start_page)

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

        request = 'SIGNUP ' + username + ' ' + password
        response = self.connection.send_request(request)
        self.validate_signup(response)

    def validate_signup(self, response):
        token = response.split('=')
        if token[0] == configs.SIGNUP_SUCCESS:
            QtWidgets.QMessageBox.about(self, 'Successful', 'Sign up Successful')
            self.back_to_start_page()
        elif token[0] == configs.SIGNUP_FAIL:
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', token[1])
        else:
            print("Wrong message")

    def back_to_start_page(self):
        print("back to start page")
        self.close()
        self.start_page = StartPage.startPage(self.connection)
        self.start_page.show()
        