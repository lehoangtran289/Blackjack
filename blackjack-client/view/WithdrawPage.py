from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class withdrawPage(QtWidgets.QWidget):
    def __init__(self, user, connection, x, y):
        super().__init__()
        uic.loadUi('./ui/withdraw.ui', self)
        self.user = user
        self.connection = connection
        self.back_button.clicked.connect(self.back)
        self.withdraw_button.clicked.connect(self.withdraw)
        self.setWindowTitle('Withdraw money')
        self.setFixedSize(800, 600)
        self.setGeometry(x, y, 800, 600)
        self.close_on_purpose = True

    def closeEvent(self, event):
        if self.close_on_purpose == False:
            event.accept()
            return
        reply = QtWidgets.QMessageBox.question(self, 'Quit', 'Are you sure you want to quit?', \
            QtWidgets.QMessageBox.Yes, QtWidgets.QMessageBox.No)
        if reply == QtWidgets.QMessageBox.Yes:
            request = 'LOGOUT ' + self.user.username
            self.connection.send(request)
            event.accept()
        else:
            event.ignore()

    def withdraw(self):
        credit_card_number = self.credit_card_entry.text().strip()
        amount = self.amount_entry.text().strip()  
        if credit_card_number == '' or amount == '':
            QtWidgets.QMessageBox.about(self, 'Invalid information', 'CreditCard and Amount field must not be empty!')
            return
        if not (credit_card_number.isdigit() and amount.isdigit()):
            QtWidgets.QMessageBox.about(self, 'Invalid information', 'CreditCard and Amount field must only contain number!')
            return

        request = 'CARDRQ ' + credit_card_number + " " + self.user.username
        response = self.connection.send_request(request)
        header = self.connection.get_header(response)
        if header == "RQFAIL":
            QtWidgets.QMessageBox.about(self, 'Invalid card', self.connection.get_message(response))
            return

        token, ok = QtWidgets.QInputDialog.getText(self, "Add/Withdraw token", "Enter token which was sent to your email")
        if ok:
            token = str(token).strip()
            request = 'WDR ' + self.user.username + ' ' + credit_card_number + ' ' + token + ' ' + amount
            response = self.connection.send_request(request)
            header = self.connection.get_header(response)
            message = self.connection.get_message(response)

            if header == 'WDRSUCCESS':
                _, balance = message.split(' ')
                self.user.balance = float(balance)
                QtWidgets.QMessageBox.about(self, 'Add Successful', 'Successful')
                self.back()
            else:
                QtWidgets.QMessageBox.about(self, 'Add Failed', message)

    def back(self): 
        self.home_page = HomePage.homePage(self.user, self.connection, self.pos().x(), self.pos().y() + 30)
        self.close_on_purpose = False
        self.close()
        self.home_page.show()