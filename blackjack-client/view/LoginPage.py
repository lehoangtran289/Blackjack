from PyQt5 import QtCore, QtWidgets, QtGui, uic
from models.User import user
from utils import configs, Connection
from view import StartPage, HomePage

class loginPage(QtWidgets.QMainWindow):
    def __init__(self, connection, x, y):
        super().__init__()
        uic.loadUi('./ui/login.ui', self)
        self.connection = connection
        self.login_button.clicked.connect(self.login)
        self.back_button.clicked.connect(self.back_to_start_page)
        self.setWindowTitle('Login')
        self.setFixedSize(800, 600)
        self.setGeometry(x, y, 800, 600)
        self.close_on_purpose = True

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
            self.home_page = HomePage.homePage(user(username, balance), self.connection, self.pos().x(), self.pos().y() + 30)
            self.close_on_purpose = False
            self.close()
            self.home_page.show()
        elif header == configs.LOGIN_FAIL:
            QtWidgets.QMessageBox.about(self, 'Log in Failed', message)
        else:
            print("Wrong message")
        
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

    def back_to_start_page(self):
        self.close_on_purpose = False
        self.close()
        self.start_page = StartPage.startPage(self.connection, self.pos().x(), self.pos().y() + 30)
        self.start_page.show()