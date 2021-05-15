from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class addPage(QtWidgets.QWidget):
    def __init__(self, user, connection):
        super().__init__()
        uic.loadUi('./ui/add.ui', self)
        self.connection = connection
        self.user = user
        self.back_button.clicked.connect(self.back)
        self.add_button.clicked.connect(self.add)

    def add(self):
        credit_card_number = self.credit_card_entry.text()
        amount = self.amount_entry.text()   
        if credit_card_number == '' or amount == '':
            QtWidgets.QMessageBox.about(self, 'Invalid information', 'CreditCard and Amount field must not be empty!')
            return
        if not (credit_card_number.isdigit() and amount.isdigit()):
            QtWidgets.QMessageBox.about(self, 'Invalid information', 'CreditCard and Amount field must only contain number!')
            return

        request = 'ADD ' + self.user.username + ' ' + credit_card_number + ' ' + amount
        response = self.connection.send_request(request)
        header, message = response.split('=')

        if header == 'ADDSUCCESS':
            _, balance = message.split(' ')
            self.user.balance = float(balance)
            QtWidgets.QMessageBox.about(self, 'Add Successful', 'Successful')
            self.back()
        else:
            QtWidgets.QMessageBox.about(self, 'Add Failed', message)

    def back(self): 
        self.home_page = HomePage.homePage(self.user, self.connection)
        self.close()
        self.home_page.show()