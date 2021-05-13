from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs
import socket
from view import HomePage

class infoPage(QtWidgets.QWidget):
    def __init__(self, user, socket, home):
        super().__init__()
        uic.loadUi('./ui/player_info.ui', self)
        self.user = user
        self.s = socket
        self.home_page = home
        
        request = 'INFO ' + self.user.username
        self.s.sendall(request.encode())
        print('send ' + request)
        response = self.s.recv(1024).decode('utf-8')
        print('recieved: ' + response)
        token = response.split('=')
        if token[0] == 'INFO':
            print("here")
            stats = token[1].split(' ')
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