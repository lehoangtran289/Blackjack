from PyQt5 import QtCore, QtWidgets, QtGui, uic
from utils import configs, Connection
import socket
from view import HomePage

class gamePage(QtWidgets.QWidget):
    def __init__(self, user, connection, room_id):
        super().__init__()
        uic.loadUi('./ui/game.ui', self)
        self.user = user
        self.connection = connection
        self.room_id = room_id
        self.bet_value = 0
        self.bet_phase = 1

        self.balance_label.setText('$' + str(self.user.balance))
        self.bet_label.setText('$' + str(self.bet_value))
        
        self.hit_button.clicked.connect(self.hit)
        self.stand_button.clicked.connect(self.stand)
        self.quit_button.clicked.connect(self.quit)
        self.chat_entry.returnPressed.connect(self.chat)
        self.bet_button.clicked.connect(self.bet)
        self.reset_bet_button.clicked.connect(self.reset_bet)
        self.add_5_button.clicked.connect(self.add_5)
        self.add_10_button.clicked.connect(self.add_10)
        self.add_20_button.clicked.connect(self.add_20)
        self.add_50_button.clicked.connect(self.add_50)


    def hit(self):
        pass

    def stand(self):
        pass

    def quit(self):
        pass

    def chat(self):
        message = self.chat_entry.text()
        self.chat_history.insertItem(0, 'You: ' + message)
        self.chat_entry.clear()
        request = 'CHAT ' + self.room_id + ' ' + self.user.username + ' ' + message
        response = self.connection.send_request(request)

    def bet(self):
        if self.bet_phase == 1:
            request = 'BET ' + str(self.room_id) + ' ' + self.user.username + ' ' + str(self.bet_value)
            response = self.connection.send_request(request)
            if self.connection.get_header(response) == 'BET':
                self.user.balance -= self.bet_value
                self.balance_label.setText('$' + str(self.user.balance))
                self.set_enable_bet_button()
            else:
                QtWidgets.QMessageBox.about(self, 'Bet Failed', self.connection.get_message(response))

    def reset_bet(self):
        self.bet_value = 0
        self.bet_label.setText('$' + str(self.bet_value))

    def add_5(self):
        self.bet_value += 5
        self.bet_label.setText('$' + str(self.bet_value))
    
    def add_10(self):
        self.bet_value += 10
        self.bet_label.setText('$' + str(self.bet_value))
        
    
    def add_20(self):
        self.bet_value += 20
        self.bet_label.setText('$' + str(self.bet_value))

    def add_50(self):
        self.bet_value += 50
        self.bet_label.setText('$' + str(self.bet_value))
  
    def set_enable_bet_button(self):
        self.bet_phase = 1 - self.bet_phase
        self.add_5_button.setEnabled(self.bet_phase)
        self.add_10_button.setEnabled(self.bet_phase)
        self.add_20_button.setEnabled(self.bet_phase)
        self.add_50_button.setEnabled(self.bet_phase)
        self.reset_bet_button.setEnabled(self.bet_phase)
        self.bet_button.setEnabled(self.bet_phase)