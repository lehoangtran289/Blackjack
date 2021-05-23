from PyQt5 import QtCore, QtWidgets, QtGui, uic
from models.User import user
from utils import configs, Connection
from view import StartPage, HomePage

class loginPage(QtWidgets.QMainWindow):
    def __init__(self, connection):
        super().__init__()
        uic.loadUi('./ui/login.ui', self)
        self.connection = connection
        self.login_button.clicked.connect(self.login)
        self.back_button.clicked.connect(self.back_to_start_page)
        self.setWindowTitle('Login')
        self.setFixedSize(640, 480)

    def login(self):
        username = self.username_entry.text()
        password = self.password_entry.text()
        if username == '' or password == '':
            QtWidgets.QMessageBox.about(self, 'Sign up Failed', 'Username and Password must not be empty!')
            return

        request = 'LOGIN ' + username + ' ' + password
        response = self.connection.send_request(request)
        self.validate_login(response, username, password)

    def validate_login(self, response, username, password):
        header = self.connection.get_header(response)
        message = self.connection.get_message(response)

        if header == configs.LOGIN_SUCCESS:
            username, balance = message.split(' ')
            self.home_page = HomePage.homePage(user(username, balance), self.connection)
            self.close()
            self.home_page.show()
        elif header == configs.LOGIN_FAIL:
            QtWidgets.QMessageBox.about(self, 'Log in Failed', message)
        else:
            print("Wrong message")

    def back_to_start_page(self):
        print("back to start page")
        self.close()
        self.start_page = StartPage.startPage(self.connection)
        self.start_page.show()