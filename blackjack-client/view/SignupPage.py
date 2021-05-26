from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import StartPage
import re

class signupPage(QtWidgets.QWidget):
    def __init__(self, connection):
        QtWidgets.QWidget.__init__(self)
        uic.loadUi('./ui/signup.ui', self)
        self.connection = connection
        self.signup_button.clicked.connect(self.signup)
        self.back_button.clicked.connect(self.back_to_start_page)
        self.setWindowTitle('Sign up')
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

    def signup(self):
        username = self.username_entry.text()
        password = self.password_entry.text()
        confirm = self.confirm_entry.text()
        if username == '' or password == '':
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', 'Username and Password must not be empty!')
            return
        if len(password) < 6 or len(password) > 20:
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', 'Password length must between 6 and 20')
            return
        if re.findall('[0-9A-Za-z]+', password)[0] != password:
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', 'Password must only contains digits and alphabet characters')
            return
        if password != confirm:
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', '2 passwords are not the same')
            return

        request = 'SIGNUP ' + username + ' ' + password
        response = self.connection.send_request(request)
        self.validate_signup(response)
       
    def validate_signup(self, response):
        header = self.connection.get_header(response)
        if header == configs.SIGNUP_SUCCESS:
            QtWidgets.QMessageBox.about(self, 'Successful', 'Sign up Successful')
            self.back_to_start_page()
        elif header == configs.SIGNUP_FAIL:
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', self.connection.get_message(response))
        else:
            print("Wrong message")

    def back_to_start_page(self):
        print("back to start page")
        self.close_on_purpose = False
        self.close()
        self.start_page = StartPage.startPage(self.connection)
        self.start_page.show()
        