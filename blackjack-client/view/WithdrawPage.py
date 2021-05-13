from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs
import socket
from view import HomePage

class withdrawPage(QtWidgets.QWidget):
    def __init__(self, user, socket):
        super().__init__()
        uic.loadUi('./ui/withdraw.ui', self)
        self.user = user
        self.s = socket
        self.back_button.clicked.connect(self.back)
        self.withdraw_button.clicked.connect(self.withdraw)

    def withdraw(self):
        credit_card_number = self.credit_card_entry.text()
        amount = self.amount_entry.text()   
        if credit_card_number == '' or amount == '':
            QtWidgets.QMessageBox.about(self, 'Invalid information', 'CreditCard and Amount field must not be empty!')
            return
        if not (credit_card_number.isdigit() and amount.isdigit()):
            QtWidgets.QMessageBox.about(self, 'Invalid information', 'CreditCard and Amount field must only contain number!')
            return

        request = 'WDR ' + self.user.username + ' ' + credit_card_number + ' ' + amount
        print('send: ' + request)
        self.s.sendall(request.encode())
        response = self.s.recv(1024).decode('utf-8')
        print('recieved: ' + response)
        header, message = response.split('=')

        if header == 'WDRSUCCESS':
            _, balance = message.split(' ')
            self.user.balance = int(balance)
            QtWidgets.QMessageBox.about(self, 'Add Successful', 'Successful')
            self.back()
        else:
            QtWidgets.QMessageBox.about(self, 'Add Failed', message)

    def back(self): 
        self.home_page = HomePage.homePage(self.user, self.s)
        self.close()
        self.home_page.show()