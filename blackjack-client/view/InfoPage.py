from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class infoPage(QtWidgets.QWidget):
    def __init__(self, user, connection, home):
        super().__init__()
        uic.loadUi('./ui/player_info.ui', self)
        self.user = user
        self.connection = connection
        self.home_page = home
        
        request = 'INFO ' + self.user.username
        response = self.connection.send_request(request)
        header, message = response.split('=')
        if header == 'INFO':
            stats = message.split(' ')
            self.username.setText(stats[0])
            self.balance.setText(stats[1])
            self.gain_lose.setText(stats[2])
            self.win.setText(stats[3])
            self.lose.setText(stats[4])
            self.push.setText(stats[5])
            self.bust.setText(stats[6])
            self.blackjack.setText(stats[7])
        else:
            QtWidgets.QMessageBox.about(self, 'Failed', token[1])

        self.back_button.clicked.connect(self.back)

    def back(self): 
        self.close()
        self.home_page.show()